// Jest manual mock — 실제 네이티브 모듈(TurboModuleRegistry)이 테스트 환경에는 없어서
// 실제 패키지를 그대로 import하면 즉시 throw한다. 테스트에 필요한 최소 표면만 스텁.
module.exports = {
  __esModule: true,
  default: {
    postMessage: jest.fn(),
    popToNative: jest.fn(),
    setNativeBackGestureAndButtonEnabled: jest.fn(),
    onMessage: jest.fn(() => ({ remove: jest.fn() })),
  },
};
