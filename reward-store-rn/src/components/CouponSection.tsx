import { Pressable, Text, View } from 'react-native';
import type { IssuedCoupon } from '../nativeModules/RewardBridge';
import type { Styles } from '../styles/createStyles';

export function CouponSection({
  styles,
  coupons,
  onSelect,
}: {
  styles: Styles;
  coupons: IssuedCoupon[];
  onSelect: (coupon: IssuedCoupon) => void;
}) {
  return (
    <View style={styles.couponSection}>
      <Text style={styles.couponSectionTitle}>보유 쿠폰</Text>
      {coupons.length === 0 && (
        <Text style={styles.couponEmptyText}>
          쿠폰함이 비어있어요 · 아메리카노 쿠폰을 교환하면 여기에 코드가 표시돼요
        </Text>
      )}
      {coupons.map(coupon => {
        const isUsed = coupon.status === 'USED';
        return (
          <Pressable
            key={coupon.code}
            disabled={isUsed}
            onPress={() => onSelect(coupon)}
            style={[styles.couponRow, isUsed && styles.couponRowUsed]}
          >
            <Text style={styles.couponCode}>{coupon.code}</Text>
            <Text style={styles.couponStatus}>
              {isUsed ? '사용 완료' : '사용 가능'}
            </Text>
          </Pressable>
        );
      })}
    </View>
  );
}
