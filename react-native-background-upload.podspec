require "json"

 json = File.read(File.join(__dir__, "package.json"))
 package = JSON.parse(json).deep_symbolize_keys

 Pod::Spec.new do |s|
   s.name = package[:name]
   s.version = package[:version]
   s.license = { type: "MIT" }
   s.homepage = "https://github.com/Vydia/react-native-background-upload"
   s.authors = package[:author]
   s.summary = package[:description]
   s.source = { git: package[:repository][:url] }
   s.source_files = "ios/**/*.{h,m,mm,swift}", "ios/*.{h,m,mm,swift}"
   s.header_dir  = 'react-native-background-upload'
   # Swift/Objective-C compatibility
   s.pod_target_xcconfig = {
     'USE_HEADERMAP' => 'YES',
     'DEFINES_MODULE' => 'YES',
     'SWIFT_COMPILATION_MODE' => 'wholemodule',
   }
   s.user_target_xcconfig = {
     "HEADER_SEARCH_PATHS" => "\"${PODS_CONFIGURATION_BUILD_DIR}/react-native-background-upload/Swift Compatibility Header\"",
   }
   s.platform = :ios, "9.0"

   s.dependency "React"
 end
