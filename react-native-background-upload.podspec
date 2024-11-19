require "json"

 json = File.read(File.join(__dir__, "package.json"))
 package = JSON.parse(json).deep_symbolize_keys
 folly_compiler_flags = '-DFOLLY_NO_CONFIG -DFOLLY_MOBILE=1 -DFOLLY_USE_LIBCPP=1 -Wno-comma -Wno-shorten-64-to-32'
 
 Pod::Spec.new do |s|
   s.name = package[:name]
   s.version = package[:version]
   s.license = { type: "MIT" }
   s.homepage = "https://github.com/Vydia/react-native-background-upload"
   s.authors = package[:author]
   s.summary = package[:description]
   s.source = { git: package[:repository][:url] }
   s.source_files = "ios/*.{h,m}"
   s.platform = :ios, "9.0"

   install_modules_dependencies(s)

   s.dependency "React"

   # This guard prevent to install the dependencies when we run `pod install` in the old architecture.
  if ENV['RCT_NEW_ARCH_ENABLED'] == '1' then
    s.compiler_flags = folly_compiler_flags + " -DRCT_NEW_ARCH_ENABLED=1"
    s.pod_target_xcconfig    = {
        "HEADER_SEARCH_PATHS" => "\"$(PODS_ROOT)/boost\"",
        "OTHER_CPLUSPLUSFLAGS" => "-DFOLLY_NO_CONFIG -DFOLLY_MOBILE=1 -DFOLLY_USE_LIBCPP=1",
        "CLANG_CXX_LANGUAGE_STANDARD" => "c++17",
        "OTHER_SWIFT_FLAGS" => "-DNEW_ARCH_ENABLED_SWIFT"
    }

    s.dependency "React-RCTFabric"
    s.dependency "React-Codegen"
    s.dependency "RCT-Folly"
    s.dependency "RCTRequired"
    s.dependency "RCTTypeSafety"
    s.dependency "ReactCommon/turbomodule/core"
  end
 end
