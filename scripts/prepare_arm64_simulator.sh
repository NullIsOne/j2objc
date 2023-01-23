

function prepareTempFolders {
  mkdir -p "$1"
  mkdir -p "$1/device"
  mkdir -p "$1/simulator"
  mkdir -p "$1/simulator-patched"
}

function prepareResultFolders {
  mkdir -p "$1"
  mkdir -p "$1/device"
  mkdir -p "$1/simulator"
  mkdir -p "$1/fat"
}

function cleanUp {
  rm -rf "$1"
}

# https://bogo.wtf/arm64-to-sim.html
# extract and pack in fat
function prepareLib {

  lib=$1
  working_dir=$2
  result_dir=$3

  lib_name="$(basename $lib .a)"

  lipo $lib -thin x86_64 -output $working_dir/simulator/${lib_name}.a
  lipo $lib -thin arm64 -output $working_dir/device/${lib_name}.a

  container=$working_dir/device/${lib_name}
  mkdir -p $container
  cd $container
  ar -x ../${lib_name}.a
  for file in *.o; do ../../../../arm64-to-sim $file; done;
  ar crv ../../simulator-patched/${lib_name}.a *.o
  cd -
  rm -rf $container

  cp -a $working_dir/device/${lib_name}.a $result_dir/device/${lib_name}.a
  lipo -create -output $result_dir/simulator/${lib_name}.a $working_dir/simulator/${lib_name}.a $working_dir/simulator-patched/${lib_name}.a
}

# making xcframework
function prepareFramework {

  framework_name=$1
  lib_name=$2
  result_dir=$3

  xcodebuild -create-xcframework \
    -library $result_dir/simulator/${lib_name}.a \
    -library $result_dir/device/${lib_name}.a \
    -output $result_dir/${framework_name}.xcframework
}

# main body
set -e

main_lib=$1
framework_name=$2

libs_path=$main_lib/build_result
temp_path=$main_lib/temp
output_path=$main_lib/libs_patched

prepareTempFolders $temp_path
prepareResultFolders $output_path

prepareLib $libs_path/lib${main_lib}.a $temp_path $output_path

prepareFramework $framework_name lib$main_lib $output_path

cleanUp $temp_path
