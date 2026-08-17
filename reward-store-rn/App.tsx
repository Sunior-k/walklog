/**
 * WalkLog Reward Store — React Native brownfield screen
 * https://github.com/facebook/react-native
 *
 * @format
 */

import { useMemo } from 'react';
import { FlatList, Pressable, StatusBar, Text, View } from 'react-native';
import {
  SafeAreaProvider,
  useSafeAreaInsets,
} from 'react-native-safe-area-context';
import { CouponDetailModal } from './src/components/CouponDetailModal';
import { CouponSection } from './src/components/CouponSection';
import { PromoCodeModal } from './src/components/PromoCodeModal';
import { RewardCard } from './src/components/RewardCard';
import { SignInBanner } from './src/components/SignInBanner';
import { useRewardStore } from './src/hooks/useRewardStore';
import { createStyles } from './src/styles/createStyles';

function App() {
  return (
    <SafeAreaProvider>
      <RewardStoreScreen />
    </SafeAreaProvider>
  );
}

function RewardStoreScreen() {
  const insets = useSafeAreaInsets();
  const store = useRewardStore();
  const styles = useMemo(() => createStyles(store.palette), [store.palette]);

  return (
    <View
      style={[
        styles.container,
        { paddingTop: insets.top, paddingBottom: insets.bottom },
      ]}
    >
      <StatusBar barStyle="light-content" />
      <View style={styles.header}>
        <View style={styles.headerRow}>
          <Text style={styles.headerTitle}>리워드 스토어</Text>
          <Pressable
            style={styles.promoCodeButton}
            onPress={store.handleOpenPromoModal}
          >
            <Text style={styles.promoCodeButtonText}>코드 등록</Text>
          </Pressable>
        </View>
        <Text style={styles.balanceLabel}>
          {store.balance === null
            ? '포인트 확인 중…'
            : `${store.balance.toLocaleString()}P`}
        </Text>
      </View>

      {store.isSignedIn === false && (
        <SignInBanner styles={styles} onPress={store.handleSignInPress} />
      )}

      <FlatList
        contentContainerStyle={styles.listContent}
        data={store.catalog}
        keyExtractor={item => item.id}
        renderItem={({ item }) => (
          <RewardCard
            styles={styles}
            item={item}
            balance={store.balance}
            isSignedIn={store.isSignedIn}
            isRedeeming={store.redeemingId === item.id}
            isOwned={store.ownedRewardIds.includes(item.id)}
            onRedeem={() => store.handleRedeem(item)}
          />
        )}
        ListFooterComponent={
          <CouponSection
            styles={styles}
            coupons={store.coupons}
            onSelect={store.setSelectedCoupon}
          />
        }
      />

      <CouponDetailModal
        styles={styles}
        coupon={store.selectedCoupon}
        isMarkingUsed={store.isMarkingUsed}
        onDismiss={() => store.setSelectedCoupon(null)}
        onMarkUsed={store.handleMarkCouponUsed}
      />

      <PromoCodeModal
        styles={styles}
        visible={store.isPromoModalVisible}
        code={store.promoCodeInput}
        isRedeeming={store.isRedeemingPromoCode}
        onChangeCode={store.setPromoCodeInput}
        onDismiss={store.handleClosePromoModal}
        onSubmit={store.handleRedeemPromoCode}
      />
    </View>
  );
}

export default App;
