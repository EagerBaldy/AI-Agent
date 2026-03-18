FROM elasticsearch:8.11.1

# Install IK Analysis plugin
RUN elasticsearch-plugin install --batch https://github.com/medcl/elasticsearch-analysis-ik/releases/download/v8.11.1/elasticsearch-analysis-ik-8.11.1.zip

# Install Pinyin Analysis plugin (optional but recommended for Chinese search)
RUN elasticsearch-plugin install --batch https://github.com/medcl/elasticsearch-analysis-pinyin/releases/download/v8.11.1/elasticsearch-analysis-pinyin-8.11.1.zip
