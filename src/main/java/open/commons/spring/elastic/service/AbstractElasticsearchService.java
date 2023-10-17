/*
 * Copyright 2022 Park Jun-Hong_(parkjunhong77@gmail.com)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 *
 * This file is generated under this project, "open-commons-spring-elastic".
 *
 * Date  : 2022. 5. 17. 오후 2:11:32
 *
 * Author: Park Jun-Hong (parkjunhong77@gmail.com)
 * 
 */

package open.commons.spring.elastic.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;

import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.client.RestClient;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.IndexedObjectInformation;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.convert.ElasticsearchConverter;
import org.springframework.data.elasticsearch.core.convert.MappingElasticsearchConverter;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.data.elasticsearch.core.query.ByQueryResponse;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQuery.OpType;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;

import open.commons.core.Result;
import open.commons.core.utils.IOUtils;
import open.commons.spring.elastic.utils.RestApiUtils;
import open.commons.spring.elastic.utils.RestClients;
import open.commons.spring.web.mvc.service.AbstractComponent;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import co.elastic.clients.elasticsearch.indices.ElasticsearchIndicesClient;
import co.elastic.clients.transport.endpoints.BooleanResponse;

/**
 * 
 * 
 * <br>
 * 
 * <pre>
 * [개정이력]
 *      날짜    	| 작성자	|	내용
 * ------------------------------------------
 * 2022. 5. 17.         박준홍     최초 작성
 * 2023. 10. 13.        박준홍     Migrate from the High Level Rest Client to Java API Client.
 * </pre>
 * 
 * @since 2022. 5. 17.
 * @version 0.2.0
 * @author Park Jun-Hong (parkjunhong77@gmail.com)
 */
public class AbstractElasticsearchService extends AbstractComponent {

    protected final ClientConfiguration esClientConfig;
    protected final RestClient restClient;
    protected final ElasticsearchClient esClient;
    protected final ElasticsearchConverter esConverter;

    /**
     * <br>
     * 
     * <pre>
     * [개정이력]
     *      날짜    	| 작성자	|	내용
     * ------------------------------------------
     * 2022. 5. 17.		박준홍			최초 작성
     * </pre>
     * 
     * @param esClientConfig
     *            Elasticsearch 클라이언트 설정
     *
     * @since 2022. 5. 17.
     * @version 0.2.0
     * @author Park Jun-Hong (parkjunhong77@gmail.com)
     */
    public AbstractElasticsearchService(@NotNull ClientConfiguration esClientConfig) {
        this(esClientConfig, null);
    }

    /**
     * 
     * <br>
     * 
     * <pre>
     * [개정이력]
     *      날짜    	| 작성자	|	내용
     * ------------------------------------------
     * 2023. 10. 13.		박준홍			최초 작성
     * </pre>
     *
     * @param esClientConfig
     *            Elasticsearch 클라이언트 설정
     * @param esConverter
     *
     * @since 2023. 10. 13.
     * @version 0.3.0
     * @author Park Jun-Hong (parkjunhong77@gmail.com)
     */
    public AbstractElasticsearchService(@NotNull ClientConfiguration esClientConfig, @Nullable ElasticsearchConverter esConverter) {
        this.esClientConfig = esClientConfig;
        this.restClient = RestClients.create(this.esClientConfig);
        this.esClient = createElasticsearchClient(this.restClient);
        this.esConverter = esConverter != null //
                ? esConverter //
                : createElasticsearchConverter();
    }

    /**
     * Elasticsearch JSON 으로 저장된 파일을 한번에 저장합니다. <br>
     * 
     * <pre>
     * [개정이력]
     *      날짜    	| 작성자	|	내용
     * ------------------------------------------
     * 2022. 9. 15.		박준홍			최초 작성
     * </pre>
     *
     * @param url
     *            Elasticsearch API 'Bulk Index' URL.<br>
     *            <ul>
     *            <li>포맷: {scheme}://{host}:{port}/{index}/_bulk
     *            <li>예시: http://192.168.0.10:80/test-index/_bulk
     *            </ul>
     * @param filepath
     *            Elasticsearch JSON bulk 파일 절대경로
     * @throws IOException
     * @throws InterruptedException
     *
     * @since 2022. 9. 15.
     * @version _._._
     * @author Park Jun-Hong (parkjunhong77@gmail.com)
     */
    public void bulkIndex(@NotNull String url, @NotNull String filepath) throws IOException, InterruptedException {

        String[] cmdarr = { "curl", "-X", "POST", url, "-H", "Content-Type: application/x-ndjson", "--data-binary", "@" + filepath };

        Process bulkProc = Runtime.getRuntime().exec(cmdarr);
        InputStream in = bulkProc.getInputStream();
        while (in.read() != -1) {
            ;
        }
        bulkProc.waitFor();

        int exitCode = bulkProc.exitValue();
        if (exitCode != 0) {
            logger.warn("Elasticsearch 'bulk' API 실행을 비정상적({})으로 종료되었습니다. command={}", exitCode, Arrays.toString(cmdarr));
        }
    }

    /**
     * 데이터를 Elasticsearch 작업 데이터로 변환하여 제공합니다. <br>
     * 
     * <pre>
     * [개정이력]
     *      날짜    	| 작성자	|	내용
     * ------------------------------------------
     * 2022. 5. 17.		박준홍			최초 작성
     * </pre>
     *
     * @param <T>
     * @param data
     * @return
     *
     * @since 2022. 5. 17.
     * @version 0.2.0
     * @author Park Jun-Hong (parkjunhong77@gmail.com)
     */
    public <T> List<IndexQuery> createBulk(@NotNull Collection<T> data) {
        return data.parallelStream().map(d -> {
            return new IndexQueryBuilder() //
                    .withObject(d)//
                    .withOpType(OpType.CREATE) //
                    .build();
        }).collect(Collectors.toList());
    }

    /**
     * 주어진 데이터를 'bulk index'를 이용하여 추가합니다. <br>
     * 
     * <pre>
     * [개정이력]
     *      날짜    	| 작성자	|	내용
     * ------------------------------------------
     * 2022. 10. 17.		박준홍			최초 작성
     * 2023. 10. 13.        박준홍     Migrate from the High Level Rest Client to Java API Client.
     * </pre>
     *
     * @param <T>
     * @param data
     *            데이터
     * @param bulkIndexFx
     *            실행 함수
     * @return
     *
     * @since 2022. 10. 17.
     * @version 0.2.0
     * @author Park Jun-Hong (parkjunhong77@gmail.com)
     */
    public <T> Supplier<Result<List<IndexedObjectInformation>>> createBulkIndexAction(@NotNull List<T> data //
            , @NotNull BiFunction<ElasticsearchOperations, List<IndexQuery>, List<IndexedObjectInformation>> bulkIndexFx) {
        Supplier<Result<List<IndexedObjectInformation>>> action = () -> {
            try {
                ElasticsearchOperations esOp = getElasticsearchOperations();
                List<IndexQuery> queries = createBulk(data);
                return Result.success(bulkIndexFx.apply(esOp, queries));
            } catch (Exception e) {
                return Result.error(e.getMessage());
            }
        };

        return action;
    }

    /**
     * 다수 개의 데이터를 생성하는 작업을 제공합니다. <br>
     * 
     * <pre>
     * [개정이력]
     *      날짜    	| 작성자	|	내용
     * ------------------------------------------
     * 2022. 5. 17.		박준홍			최초 작성
     * </pre>
     *
     * @param <T>
     * @param data
     *            데이터
     * @param type
     *            IndexEntity class
     * @return
     *
     * @since 2022. 5. 17.
     * @version 0.2.0
     * @author Park Jun-Hong (parkjunhong77@gmail.com)
     */
    public <T> Supplier<Result<List<IndexedObjectInformation>>> createBulkIndexAction(@NotNull List<T> data, @NotNull Class<T> type) {
        return createBulkIndexAction(data, (esOp, queries) -> esOp.bulkIndex(queries, type));
    }

    /**
     * 다수 개의 데이터를 생성하는 작업을 제공합니다. <br>
     * 
     * <br>
     * 
     * <pre>
     * [개정이력]
     *      날짜    	| 작성자	|	내용
     * ------------------------------------------
     * 2022. 10. 13.		박준홍			최초 작성
     * </pre>
     *
     * @param <T>
     * @param data
     *            데이터
     * @param indexName
     *            'index' 이름
     * @return
     *
     * @since 2022. 10. 13.
     * @version 0.2.0
     * @author Park Jun-Hong (parkjunhong77@gmail.com)
     */
    public <T> Supplier<Result<List<IndexedObjectInformation>>> createBulkIndexAction(List<T> data, String indexName) {
        return createBulkIndexAction(data, (esOp, queries) -> esOp.bulkIndex(queries, IndexCoordinates.of(indexName)));
    }

    /**
     * 데이터를 Elasticsearch JSON 문자열로 변환하여 저장한 후, 저장한 파일정보를 제공합니다. <br>
     * 
     * <pre>
     * [개정이력]
     *      날짜    	| 작성자	|	내용
     * ------------------------------------------
     * 2022. 9. 15.		박준홍			최초 작성
     * </pre>
     *
     * @param <T>
     *            데이터 타입
     * @param data
     *            데이터
     * @param tempfilePrefix
     *            임시파일 접두어
     * @param tempfileSuffix
     *            임시파일 접미어
     * @return 임시파일 절대 경로
     * @throws IOException
     *             파일 생성시 오류가 발생한 경우
     *
     * @since 2022. 9. 15.
     * @version 0.2.0
     * @author Park Jun-Hong (parkjunhong77@gmail.com)
     */
    public <T> String createBulkJSONTempFile(@NotNull List<T> data, @NotNull String tempfilePrefix, @NotNull String tempfileSuffix) throws IOException {
        // #1. bulk JSON 문자열 생성
        String bulkNDJsonStr = createNDJsonString(data);
        Reader reader = new BufferedReader(new StringReader(bulkNDJsonStr));

        // #2. bulk JSON 파일 생성
        File tmpfile = File.createTempFile(tempfilePrefix, tempfileSuffix);
        Writer writer = new FileWriter(tmpfile);

        IOUtils.transfer(reader, true, writer, true, IOUtils.BUFFER_SIZE_1MB);
        return tmpfile.getAbsolutePath();
    }

    /**
     * {@link ElasticsearchClient}를 제공합니다.<br>
     * 하위 클래스는 이 메소드를 overriding 하여 목적에 맞게 구현합니다.
     * 
     * <pre>
     * [개정이력]
     *      날짜    	| 작성자	|	내용
     * ------------------------------------------
     * 2023. 10. 16.		박준홍			최초 작성
     * </pre>
     *
     * @param restClient
     * @return
     *
     * @since 2023. 10. 16.
     * @version 0.2.0
     * @author Park Jun-Hong (parkjunhong77@gmail.com)
     */
    protected ElasticsearchClient createElasticsearchClient(@NotNull RestClient restClient) {
        return RestClients.createElasticsearchClient(restClient, null);
    }

    private ElasticsearchConverter createElasticsearchConverter() {
        MappingElasticsearchConverter mappingElasticsearchConverter = new MappingElasticsearchConverter(new SimpleElasticsearchMappingContext());
        mappingElasticsearchConverter.afterPropertiesSet();
        return mappingElasticsearchConverter;
    }

    /**
     * 주어진 이름과 매핑 정보를 이용하여 Index 를 생성합니다. <br>
     * 
     * <pre>
     * [개정이력]
     *      날짜    	| 작성자	|	내용
     * ------------------------------------------
     * 2022. 10. 13.		박준홍			최초 작성
     * 2023. 10. 13.        박준홍     Migrate from the High Level Rest Client to Java API Client.
     * </pre>
     *
     * @param indexName
     * @param source
     *            'settings' and 'mapping' 문자열
     * @return 'index' 생성 결과
     *
     * @since 2022. 10. 13.
     * @version 0.2.0
     * @author Park Jun-Hong (parkjunhong77@gmail.com)
     */
    public Result<String> createIndex(@NotNull String indexName, @NotNull String source) {

        try {
            ElasticsearchIndicesClient idxClient = this.esClient.indices();
            BooleanResponse res = idxClient.exists(bld -> bld.index(indexName));
            boolean exists = res.value();

            if (exists) {
                logger.debug("* * * '{}' exist. index={}", exists ? "ALREADY" : "DO NOT", indexName);
                return Result.success(indexName);
            } else {
                if (source != null) {
                    Reader sourceReader = new StringReader(source);
                    CreateIndexResponse resCreateIndex = idxClient.create(b -> b.index(indexName).withJson(sourceReader));
                    logger.info("* * * 'CREATE' an index, {}. info={}", indexName, resCreateIndex);
                    return Result.success(indexName);
                } else {
                    String failedMsg = String.format("* * * 'No' source(settins, mappings, etc) for %s. info=%s", indexName, source);
                    logger.warn("{}", failedMsg);
                    return Result.error(failedMsg);
                }
            }
        } catch (ElasticsearchStatusException e) {
            String exMsg = e.toString();
            if (exMsg != null && exMsg.toLowerCase().contains("already exists")) {
                logger.debug("* * * '{}' ALREADY exist. index={}", indexName);
                return Result.success(indexName);
            } else {
                String errMsg = String.format("'%s' index 조회/생성 시 오류가 발생하였습니다. 원인=%s", indexName, e.getMessage());
                logger.error("{}", errMsg, e);
                return Result.error(errMsg);
            }
        } catch (IOException e) {
            String errMsg = String.format("'%s' index 조회/생성 시 오류가 발생하였습니다. 원인=%s", indexName, e.getMessage());
            logger.error("{}", errMsg, e);
            return Result.error(errMsg);
        }
    }

    /**
     * 데이터를 Elasticsearch Bulk JSON 형태로 변환하여 제공합니다. <br>
     * 
     * <pre>
     * [개정이력]
     *      날짜    	| 작성자	|	내용
     * ------------------------------------------
     * 2022. 9. 15.		박준홍			최초 작성
     * </pre>
     *
     * @param <T>
     * @param data
     * @return
     *
     * @since 2022. 9. 15.
     * @version 0.2.0
     * @author Park Jun-Hong (parkjunhong77@gmail.com)
     */
    public <T> String createNDJsonString(@NotNull List<T> data) {
        return data.parallelStream()
                // 단일 객체를 문자열로 변환
                .map(d -> RestApiUtils.toNDJsonString(d))
                // 'null' 확인
                .filter(s -> s != null)
                // 하나의 문자열로 통합
                .collect(Collectors.joining());
    }

    /**
     * 데이터 삭제하고 결과를 제공합니다. <br>
     * 
     * <pre>
     * [개정이력]
     *      날짜    	| 작성자	|	내용
     * ------------------------------------------
     * 2022. 5. 17.		박준홍			최초 작성
     * 2023. 10. 13.        박준홍     Migrate from the High Level Rest Client to Java API Client.
     * </pre>
     *
     * @param query
     * @param clazz
     * @return
     *
     * @since 2022. 5. 17.
     * @version 0.2.0
     * @author Park Jun-Hong (parkjunhong77@gmail.com)
     */
    public ByQueryResponse delete(@NotNull Query query, @NotNull Class<?> clazz) {
        ElasticsearchOperations esOp = getElasticsearchOperations();
        return esOp.delete(query, clazz);
    }

    /**
     * {@link RestClient}를 이용하여 생성된 {@link ElasticsearchTemplate} 연동 객체를 제공합니다. <br>
     * 
     * <pre>
     * [개정이력]
     *      날짜    	| 작성자	|	내용
     * ------------------------------------------
     * 2022. 5. 17.		박준홍			최초 작성
     * 2023. 10. 13.        박준홍     Migrate from the High Level Rest Client to Java API Client.
     * </pre>
     *
     * @return
     *
     * @since 2022. 5. 17.
     * @version 0.2.0
     * @author Park Jun-Hong (parkjunhong77@gmail.com)
     */
    public ElasticsearchOperations getElasticsearchOperations() {
        return new ElasticsearchTemplate(this.esClient, this.esConverter);
    }

    /**
     * 검색된 데이터만 제공합니다. <br>
     * 
     * <pre>
     * [개정이력]
     *      날짜    	| 작성자	|	내용
     * ------------------------------------------
     * 2022. 5. 17.		박준홍			최초 작성
     * </pre>
     *
     * @param <E>
     * @param query
     * @param type
     * @return
     *
     * @since 2022. 5. 17.
     * @version 0.2.0
     * @author Park Jun-Hong (parkjunhong77@gmail.com)
     */
    public <E> List<E> search(@NotNull Query query, @NotNull Class<E> type) {
        SearchHits<E> searchHits = searchHits(query, type);
        return searchHits.stream() //
                .map(hit -> hit.getContent())//
                .collect(Collectors.toList());
    }

    /**
     * 데이터를 검색하고 결과를 제공합니다. <br>
     * 
     * <pre>
     * [개정이력]
     *      날짜    	| 작성자	|	내용
     * ------------------------------------------
     * 2022. 5. 17.		박준홍			최초 작성
     * </pre>
     *
     * @param <E>
     * @param query
     * @param type
     * @return
     *
     * @since 2022. 5. 17.
     * @version 0.2.0
     * @author Park Jun-Hong (parkjunhong77@gmail.com)
     */
    public <E> SearchHits<E> searchHits(@NotNull Query query, @NotNull Class<E> type) {
        ElasticsearchOperations esOp = getElasticsearchOperations();
        return esOp.search(query, type);
    }

}
