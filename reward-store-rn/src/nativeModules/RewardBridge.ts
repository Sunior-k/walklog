import { NativeModules } from 'react-native';

export type RedeemResult =
  | 'SUCCESS'
  | 'INSUFFICIENT_BALANCE'
  | 'SIGN_IN_REQUIRED'
  | 'ALREADY_OWNED'
  | 'REDEMPTION_FAILED';

export type CouponStatus = 'ISSUED' | 'USED';

export interface IssuedCoupon {
  code: string;
  status: CouponStatus;
  createdAtEpochMillis: number;
}

export interface ThemeState {
  isDarkMode: boolean;
  isPremiumActive: boolean;
}

export interface RewardCatalogItem {
  id: string;
  emoji: string;
  title: string;
  description: string;
  cost: number;
}

export type PromoCodeRedeemStatus =
  | 'SUCCESS'
  | 'ALREADY_REDEEMED'
  | 'INVALID_CODE'
  | 'SIGN_IN_REQUIRED'
  | 'UNKNOWN_ERROR';

export interface PromoCodeRedeemResponse {
  status: PromoCodeRedeemStatus;
  pointsAwarded?: number;
}

/**
 * app/src/main/java/com/river/walklog/reactbridge/RewardBridgeModule.kt 와 메서드 시그니처가
 * 반드시 일치해야 함 — 공유 스키마 수단이 없어 양쪽을 수동으로 동기화한다.
 */
interface WalkLogRewardBridgeModule {
  getPointsBalance(): Promise<number>;
  redeemReward(itemId: string, cost: number): Promise<RedeemResult>;
  isSignedIn(): Promise<boolean>;
  navigateToSignIn(): Promise<void>;
  getIssuedCoupons(): Promise<IssuedCoupon[]>;
  markCouponUsed(code: string): Promise<boolean>;
  getThemeState(): Promise<ThemeState>;
  getRewardCatalog(): Promise<RewardCatalogItem[]>;
  getOwnedRewardIds(): Promise<string[]>;
  redeemPromoCode(code: string): Promise<PromoCodeRedeemResponse>;
}

const { WalkLogRewardBridge } = NativeModules as {
  WalkLogRewardBridge: WalkLogRewardBridgeModule;
};

export default WalkLogRewardBridge;
