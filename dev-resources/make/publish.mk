BLOG_DIR = $(ROOT_DIR)/blog
CSS_DIR = $(BLOG_DIR)/css
REPO = $(shell git config --get remote.origin.url)
LOCAL_BLOG_HOST = localhost
LOCAL_BLOG_PORT = $(lastword $(shell grep dev-port project.clj))
COLOUR_THEME = frmx

blog: blog-clean blog-local

blog-clean:
	@echo "\nCleaning old blog build ..."

blog-data:
	@rm -rf blog/data
	@cp -r resources/data blog/

blog-pre:
	@echo "\nBuilding blog ...\n"
	@$(MAKE) blog-data

blog-css:
	@lessc $(LESS_DIR)/styles-$(COLOUR_THEME).less $(CSS_DIR)/styles.css

blog-clojure:
	@ob gen
	@echo

blog-local: blog-pre blog-css blog-clojure

blog-dev-gen: blog
	@echo "\nRunning blog server from generated static content ..."
	@echo "URL: http://$(LOCAL_BLOG_HOST):$(LOCAL_BLOG_PORT)/blog"
	@lein simpleton $(LOCAL_BLOG_PORT) file :from $(ROOT_DIR)

blog-dev:
	@echo "\nRunning blog server from code ..."
	@echo "URL: http://$(LOCAL_BLOG_HOST):$(LOCAL_BLOG_PORT)/blog"
	@ob run

.PHONY: blog

commit-content:
	@git commit blog -m "Regen'ed content." || echo "Nothing to commit."
	@git push origin master || echo "Nothing to push."
