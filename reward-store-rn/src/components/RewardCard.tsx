import { Pressable, Text, View } from 'react-native';
import type { RewardCatalogItem } from '../nativeModules/RewardBridge';
import type { Styles } from '../styles/createStyles';

export function RewardCard({
  styles,
  item,
  balance,
  isSignedIn,
  isRedeeming,
  isOwned,
  onRedeem,
}: {
  styles: Styles;
  item: RewardCatalogItem;
  balance: number | null;
  isSignedIn: boolean | null;
  isRedeeming: boolean;
  isOwned: boolean;
  onRedeem: () => void;
}) {
  // 로그아웃 상태에서는 잔액(0P)이 실제 소유 포인트를 의미하지 않으므로
  // 잔액 부족으로 막지 않고 눌러서 로그인 유도 흐름(SIGN_IN_REQUIRED)으로 보냄.
  const canAfford =
    isSignedIn === false || (balance !== null && balance >= item.cost);
  const isDisabled = isRedeeming || isOwned || !canAfford;

  const label = isOwned ? '보유 중' : isRedeeming ? '처리 중…' : '교환하기';

  return (
    <View style={styles.card}>
      <Text style={styles.cardEmoji}>{item.emoji}</Text>
      <View style={styles.cardBody}>
        <Text style={styles.cardTitle}>{item.title}</Text>
        <Text style={styles.cardDescription}>{item.description}</Text>
        <Text style={styles.cardCost}>{item.cost.toLocaleString()}P</Text>
      </View>
      <Pressable
        disabled={isDisabled}
        onPress={onRedeem}
        style={[styles.redeemButton, isDisabled && styles.redeemButtonDisabled]}
      >
        <Text style={styles.redeemButtonText}>{label}</Text>
      </Pressable>
    </View>
  );
}
