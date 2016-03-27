CSS=build/css/style.css
APP=build/js/compiled/earth_vr.js
IDX=build/index.html
IMG=build/img/bump.jpg build/img/clouds.png build/img/earth.jpg build/img/lights.jpg build/img/specular.jpg build/img/stars.png
IMG_PUBLIC=$(subst build,resources/public,$(IMG))
ME=$(shell basename $(shell pwd))
REPO=git@github.com:retrogradeorbit/earth-vr.git

all: $(APP) $(CSS) $(IDX) $(IMG) $(SFX) $(MUSIC)

$(CSS): resources/public/css/style.css
	mkdir -p $(dir $(CSS))
	cp $< $@

$(APP): src/**/** project.clj
	rm -f $(APP)
	lein cljsbuild once min

$(IDX): resources/public/index.html
	cp $< $@

$(IMG): $(IMG_PUBLIC)
	mkdir -p build/img/
	cp $? build/img/

$(SFX): $(SFX_SOURCE)
	mkdir -p build/sfx/
	cp $? build/sfx/

$(MUSIC): $(MUSIC_SOURCE)
	mkdir -p build/music/
	cp $? build/music/

clean:
	lein clean
	rm -rf $(CSS) $(APP) $(IDX) $(IMG) $(SFX) $(MUSIC)

test-server: all
	cd build && python -m SimpleHTTPServer

setup-build-folder:
	git clone $(REPO) build/
	cd build && git checkout gh-pages

create-build-folder:
	git clone $(REPO) build/
