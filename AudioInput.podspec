Pod::Spec.new do |s|
  s.name = 'AudioInput'
  s.version = '2.0.0'
  s.summary = 'Audio input capture plugin for Capacitor'
  s.license = 'MIT'
  s.homepage = 'https://github.com/edimuj/cordova-plugin-audioinput'
  s.author = 'Edin Mujkanovic'
  s.source = { :git => 'https://github.com/edimuj/cordova-plugin-audioinput', :tag => s.version.to_s }
  s.source_files = 'ios/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}', 'src/ios/**/*.{h,m}'
  s.ios.deployment_target  = '13.0'
  s.dependency 'Capacitor'
  s.swift_version = '5.1'
  s.frameworks = 'AVFoundation', 'AudioToolbox', 'CoreAudio', 'Accelerate'
end
