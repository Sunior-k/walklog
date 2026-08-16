import { useCallback, useEffect, useState } from 'react';
import { Alert, AppState } from 'react-native';
import ReactNativeBrownfield from '@callstack/react-native-brownfield';
import RewardBridge, {
  type IssuedCoupon,
  type PromoCodeRedeemResponse,
  type RedeemResult,
  type RewardCatalogItem,
  type ThemeState,
} from '../nativeModules/RewardBridge';
import { fallbackRewardCatalog, RewardCatalogIds } from '../data/rewardCatalog';
import { resolveWalkLogPalette } from '../theme/walklogColors';

function showSignInRequiredAlert(message: string, onSignIn: () => void) {
  Alert.alert('로그인이 필요해요', message, [
    { text: '취소', style: 'cancel' },
    { text: '로그인하기', onPress: onSignIn },
  ]);
}

export function useRewardStore() {
  const [theme, setTheme] = useState<ThemeState>({
    isDarkMode: true,
    isPremiumActive: false,
  });
  const [isSignedIn, setIsSignedIn] = useState<boolean | null>(null);
  const [catalog, setCatalog] = useState<RewardCatalogItem[]>(
    fallbackRewardCatalog,
  );
  const [balance, setBalance] = useState<number | null>(null);
  const [redeemingId, setRedeemingId] = useState<string | null>(null);
  const [coupons, setCoupons] = useState<IssuedCoupon[]>([]);
  const [selectedCoupon, setSelectedCoupon] = useState<IssuedCoupon | null>(
    null,
  );
  const [isMarkingUsed, setIsMarkingUsed] = useState(false);
  const [isPromoModalVisible, setIsPromoModalVisible] = useState(false);
  const [promoCodeInput, setPromoCodeInput] = useState('');
  const [isRedeemingPromoCode, setIsRedeemingPromoCode] = useState(false);
  const [ownedRewardIds, setOwnedRewardIds] = useState<string[]>([]);

  const palette = resolveWalkLogPalette(theme.isDarkMode, theme.isPremiumActive);

  const loadTheme = useCallback(async () => {
    try {
      setTheme(await RewardBridge.getThemeState());
    } catch {
      // 테마 조회 실패 시 기본 다크 팔레트 유지.
    }
  }, []);

  const loadCatalog = useCallback(async () => {
    try {
      const items = await RewardBridge.getRewardCatalog();
      if (items.length > 0) setCatalog(items);
    } catch {
      // Firestore 조회 실패 시 오프라인 폴백 카탈로그 유지.
    }
  }, []);

  const loadSignInState = useCallback(async () => {
    try {
      setIsSignedIn(await RewardBridge.isSignedIn());
    } catch {
      setIsSignedIn(false);
    }
  }, []);

  const loadBalance = useCallback(async () => {
    try {
      setBalance(await RewardBridge.getPointsBalance());
    } catch {
      Alert.alert('오류', '포인트를 불러오지 못했어요.');
    }
  }, []);

  const loadCoupons = useCallback(async () => {
    try {
      setCoupons(await RewardBridge.getIssuedCoupons());
    } catch {
      // 쿠폰 목록은 부가 정보라 조회 실패해도 화면 진입 자체를 막지 않음.
    }
  }, []);

  const loadOwnedRewardIds = useCallback(async () => {
    try {
      setOwnedRewardIds(await RewardBridge.getOwnedRewardIds());
    } catch {
      // 보유 여부 조회 실패 시 이전 값 유지 — 버튼이 다시 활성으로 보여도 재교환은
      // 백엔드에서 AlreadyOwned로 막힘.
    }
  }, []);

  const refreshAll = useCallback(() => {
    loadTheme();
    loadCatalog();
    loadSignInState();
    loadBalance();
    loadCoupons();
    loadOwnedRewardIds();
  }, [
    loadTheme,
    loadCatalog,
    loadSignInState,
    loadBalance,
    loadCoupons,
    loadOwnedRewardIds,
  ]);

  useEffect(() => {
    refreshAll();
    // 로그인 화면에서 돌아왔을 때(앱이 다시 foreground) 로그인/잔액 상태 재조회.
    const subscription = AppState.addEventListener('change', state => {
      if (state === 'active') {
        loadSignInState();
        loadBalance();
      }
    });
    return () => subscription.remove();
  }, [refreshAll, loadSignInState, loadBalance]);

  useEffect(() => {
    // 네이티브가 이 RN 화면의 루트 View를 재사용하므로(RewardStoreViewHost),
    // 화면을 나갔다가 다시 들어와도 이 컴포넌트는 리마운트되지 않음 — 네이티브 쪽
    // Fragment가 재진입 시점에 보내주는 메시지로 잔액/쿠폰/로그인 상태를 다시 조회.
    const subscription = ReactNativeBrownfield.onMessage(event => {
      const data = event.data;
      if (data && typeof data === 'object' && 'type' in data && data.type === 'SCREEN_FOCUSED') {
        refreshAll();
      }
    });
    return () => subscription.remove();
  }, [refreshAll]);

  const handleSignInPress = useCallback(async () => {
    try {
      await RewardBridge.navigateToSignIn();
    } catch {
      Alert.alert('오류', '로그인 화면으로 이동하지 못했어요.');
    }
  }, []);

  const handleRedeem = useCallback(
    async (item: RewardCatalogItem) => {
      setRedeemingId(item.id);
      try {
        const result: RedeemResult = await RewardBridge.redeemReward(
          item.id,
          item.cost,
        );
        switch (result) {
          case 'SUCCESS':
            await loadBalance();
            if (item.id === RewardCatalogIds.COFFEE_COUPON) {
              await loadCoupons();
            }
            if (item.id === RewardCatalogIds.THEME_PACK) {
              await loadTheme();
            }
            await loadOwnedRewardIds();
            break;
          case 'INSUFFICIENT_BALANCE':
            Alert.alert('포인트 부족', '포인트가 부족해서 교환할 수 없어요.');
            break;
          case 'SIGN_IN_REQUIRED':
            showSignInRequiredAlert(
              '리워드 교환은 로그인 후 이용할 수 있어요.',
              handleSignInPress,
            );
            break;
          case 'ALREADY_OWNED':
            await loadOwnedRewardIds();
            Alert.alert('이미 보유하고 있어요', '한 번만 교환할 수 있는 상품이에요.');
            break;
          case 'REDEMPTION_FAILED':
            await loadBalance();
            Alert.alert('교환 실패', '포인트는 환불됐어요. 잠시 후 다시 시도해주세요.');
            break;
        }
      } catch {
        Alert.alert('교환 실패', '잠시 후 다시 시도해주세요.');
      } finally {
        setRedeemingId(null);
      }
    },
    [loadBalance, loadCoupons, loadTheme, loadOwnedRewardIds, handleSignInPress],
  );

  const handleMarkCouponUsed = useCallback(async () => {
    if (!selectedCoupon) return;
    setIsMarkingUsed(true);
    try {
      const success = await RewardBridge.markCouponUsed(selectedCoupon.code);
      if (success) {
        await loadCoupons();
        setSelectedCoupon(null);
      } else {
        Alert.alert('처리 실패', '이미 사용된 쿠폰이거나 오류가 발생했어요.');
      }
    } catch {
      Alert.alert('처리 실패', '잠시 후 다시 시도해주세요.');
    } finally {
      setIsMarkingUsed(false);
    }
  }, [selectedCoupon, loadCoupons]);

  const handleOpenPromoModal = useCallback(() => {
    setPromoCodeInput('');
    setIsPromoModalVisible(true);
  }, []);

  const handleClosePromoModal = useCallback(() => {
    if (isRedeemingPromoCode) return;
    setIsPromoModalVisible(false);
  }, [isRedeemingPromoCode]);

  const handleRedeemPromoCode = useCallback(async () => {
    const code = promoCodeInput.trim();
    if (code.length === 0) return;
    setIsRedeemingPromoCode(true);
    try {
      const response: PromoCodeRedeemResponse =
        await RewardBridge.redeemPromoCode(code);
      switch (response.status) {
        case 'SUCCESS':
          setIsPromoModalVisible(false);
          await loadBalance();
          Alert.alert(
            '코드 등록 완료',
            `${(response.pointsAwarded ?? 0).toLocaleString()}P를 받았어요.`,
          );
          break;
        case 'ALREADY_REDEEMED':
          Alert.alert('이미 등록된 코드', '이 코드는 이미 등록했어요.');
          break;
        case 'INVALID_CODE':
          Alert.alert('등록 실패', '유효하지 않거나 만료된 코드예요.');
          break;
        case 'SIGN_IN_REQUIRED':
          setIsPromoModalVisible(false);
          showSignInRequiredAlert(
            '코드 등록은 로그인 후 이용할 수 있어요.',
            handleSignInPress,
          );
          break;
        case 'UNKNOWN_ERROR':
          Alert.alert('등록 실패', '잠시 후 다시 시도해주세요.');
          break;
      }
    } catch {
      Alert.alert('등록 실패', '잠시 후 다시 시도해주세요.');
    } finally {
      setIsRedeemingPromoCode(false);
    }
  }, [promoCodeInput, loadBalance, handleSignInPress]);

  return {
    palette,
    isSignedIn,
    catalog,
    balance,
    redeemingId,
    ownedRewardIds,
    coupons,
    selectedCoupon,
    setSelectedCoupon,
    isMarkingUsed,
    isPromoModalVisible,
    promoCodeInput,
    setPromoCodeInput,
    isRedeemingPromoCode,
    handleSignInPress,
    handleRedeem,
    handleMarkCouponUsed,
    handleOpenPromoModal,
    handleClosePromoModal,
    handleRedeemPromoCode,
  };
}
