/**
 * core/designsystem/.../foundation/WalkLogColor.kt, Theme.kt의 팔레트를 그대로 미러링.
 * 공유 스키마 수단이 없어 Kotlin 쪽 값이 바뀌면 이 파일도 수동으로 맞춰야 함
 * (rewardCatalog.ts의 RewardCatalogIds와 동일한 동기화 패턴).
 */
const brand = {
  primary: '#F5C400',
  primaryDark: '#E0B000',
  primaryLight: '#FFE066',
  primaryContainer: '#FFF3C4',
  primaryContainerDark: '#3D3000',
  secondary: '#4666A8',
  secondaryContainer: '#1A237E',
  success: '#4CAF50',
  error: '#FF3131',
  errorDark: '#CC0000',
  staticBlack: '#000000',
  staticWhite: '#FFFFFF',
} as const;

const gray = {
  gray50: '#F8F8F8',
  gray100: '#EDEDED',
  gray200: '#DADADA',
  gray300: '#C4C4C4',
  gray500: '#757575',
  gray700: '#4F4F4F',
  gray900: '#212121',
} as const;

const premium = {
  background: '#0C1A47',
  surface: '#13224F',
  surfaceVariant: '#1B2C5C',
  outlineVariant: '#2E4380',
} as const;

export interface WalkLogPalette {
  background: string;
  surface: string;
  surfaceVariant: string;
  border: string;
  primary: string;
  onPrimary: string;
  textPrimary: string;
  textSecondary: string;
  danger: string;
}

const lightPalette: WalkLogPalette = {
  background: gray.gray50,
  surface: brand.staticWhite,
  surfaceVariant: gray.gray100,
  border: gray.gray200,
  primary: brand.primary,
  onPrimary: brand.staticBlack,
  textPrimary: gray.gray900,
  textSecondary: gray.gray500,
  danger: brand.error,
};

const darkPalette: WalkLogPalette = {
  background: gray.gray900,
  surface: gray.gray700,
  surfaceVariant: gray.gray700,
  border: gray.gray500,
  primary: brand.primary,
  onPrimary: brand.staticBlack,
  textPrimary: brand.staticWhite,
  textSecondary: gray.gray300,
  danger: brand.error,
};

const premiumPalette: WalkLogPalette = {
  background: premium.background,
  surface: premium.surface,
  surfaceVariant: premium.surfaceVariant,
  border: premium.outlineVariant,
  primary: brand.primary,
  onPrimary: brand.staticBlack,
  textPrimary: brand.staticWhite,
  textSecondary: gray.gray300,
  danger: brand.error,
};

export function resolveWalkLogPalette(
  isDarkMode: boolean,
  isPremiumActive: boolean,
): WalkLogPalette {
  if (isPremiumActive) return premiumPalette;
  return isDarkMode ? darkPalette : lightPalette;
}

export const WalkLogColor = { ...brand, ...gray, ...premium };
