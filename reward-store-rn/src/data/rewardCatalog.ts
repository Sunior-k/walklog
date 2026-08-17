import type { RewardCatalogItem } from '../nativeModules/RewardBridge';

/**
 * core/model/src/main/java/com/river/walklog/core/model/RewardCatalogIds.kt와 값이
 * 반드시 일치해야 함 — 공유 스키마 수단이 없어 양쪽을 수동으로 동기화한다.
 */
export const RewardCatalogIds = {
  COFFEE_COUPON: 'coffee-coupon',
  BADGE_GOLD: 'walk-badge-gold',
  DONATION: 'donation-500',
  THEME_PACK: 'theme-pack',
} as const;

/**
 * 실제 가격/판매 여부는 Firestore `rewardCatalog` 컬렉션에서 네이티브 브릿지
 * (getRewardCatalog)로 받아온다. 이 배열은 그 조회가 실패했을 때만 쓰는 오프라인
 * 폴백이라 실제 가격과 다를 수 있음.
 */
export const fallbackRewardCatalog: RewardCatalogItem[] = [
  {
    id: 'coffee-coupon',
    emoji: '☕',
    title: '아메리카노 쿠폰',
    description: '가까운 카페에서 사용 가능한 아메리카노 교환권',
    cost: 500,
  },
  {
    id: 'walk-badge-gold',
    emoji: '🥇',
    title: '골드 워커 뱃지',
    description: '프로필에 표시되는 스페셜 뱃지',
    cost: 200,
  },
  {
    id: 'donation-500',
    emoji: '💚',
    title: '기부 500P',
    description: '적립한 포인트를 걷기 캠페인에 기부',
    cost: 500,
  },
  {
    id: 'theme-pack',
    emoji: '🎨',
    title: '테마 팩',
    description: '앱 홈 화면 전용 시즌 테마',
    cost: 800,
  },
];
