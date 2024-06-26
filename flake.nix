{
  inputs = {
    nixpkgs.url = "github:NixOS/nixpkgs/nixos-unstable";
    nixpkgs-stable.url = "github:NixOS/nixpkgs/nixos-23.11";
  };

  outputs = { self, nixpkgs, nixpkgs-stable }:

    let
      pkgs = nixpkgs.legacyPackages.x86_64-linux;
      pkgsStable = nixpkgs-stable.legacyPackages.x86_64-linux;

      libs = (with pkgs; [
        libGL
        glfw
        stdenv.cc.cc.lib
      ]);

      java = pkgs.jdk11;
      maven = pkgsStable.maven;

      javaDeps = ([
        java
        maven
      ]);
    in
    {
      devShell.x86_64-linux = pkgs.mkShell {
        packages = [ ];
        buildInputs = libs ++ javaDeps;
        LD_LIBRARY_PATH = pkgs.lib.makeLibraryPath libs;
        JAVA_HOME = java.home;
      };

      packages.x86_64-linux.default = maven.buildMavenPackage rec {
        pname = "sayle";
        version = "main";

        src = pkgs.fetchFromGitHub {
          owner = "thinnerthinker";
          repo = pname;
          rev = version;
          hash = "sha256-ASH79ULjyxsifLLYVe+PArzZBiB/7x+drC5T3FdqikI=";
        };

        mvnHash = "sha256-Hb9IHasCyYrNMh9in1cg/1OAvEbYfb9m13aerfO4jPk=";

        buildInputs = libs ++ javaDeps ++ [ pkgs.makeWrapper ];

        installPhase = ''
          mkdir -p $out/jar
          cp target/sayle-1.0-SNAPSHOT.jar $out/jar/

          makeWrapper ${java}/bin/java $out/bin/sayle \
            --add-flags "-cp $out/jar/sayle-1.0-SNAPSHOT.jar com.yallo.sayle.sandbox.Main" \
            --set JAVA_HOME "${java.home}" \
            --prefix LD_LIBRARY_PATH : ${pkgs.lib.makeLibraryPath libs}
        '';
      };
    };
}
