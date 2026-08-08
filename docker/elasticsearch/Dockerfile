# 공식 이미지에는 nori(한국어 형태소 분석) 플러그인이 기본 포함되어 있지 않아 직접 설치한다.
# 버전은 co.elastic.clients:elasticsearch-java 클라이언트 버전(build.gradle 의존성 트리 기준)과 맞춘다.
ARG ES_VERSION=9.4.2
FROM docker.elastic.co/elasticsearch/elasticsearch:${ES_VERSION}

RUN bin/elasticsearch-plugin install --batch analysis-nori
