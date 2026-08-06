package com.kbt.amumal.domain.search.repository;

import com.kbt.amumal.domain.search.entity.SearchIndexDirty;
import com.kbt.amumal.global.config.QueryDSLConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(QueryDSLConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class SearchIndexDirtyRepositoryTest {

    @Autowired
    SearchIndexDirtyRepository repository;

    @Test
    void 같은_게시글의_변경은_하나의_Dirty행에_병합한다() {
        repository.markDirty(42);
        repository.markDirty(42);

        SearchIndexDirty dirty = repository.findById(42).orElseThrow();

        assertThat(dirty.getVersion()).isEqualTo(2);
        assertThat(repository.count()).isEqualTo(1);
    }
}
