package until.the.eternity.dcs.domain.board.entity;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class BoardTest {
    static Board board;
    static String name = "board name";
    static String description = "board description";
    static String topCategory = "top category";
    static String subCategory = "sub category";

    @BeforeEach
    void setUp() {
        board =
                Board.builder()
                        .id(1L)
                        .name(name)
                        .description(description)
                        .topCategory(topCategory)
                        .subCategory(subCategory)
                        .createdBy(1L)
                        .build();
    }

    @Test
    @DisplayName("update 는 정보를 변경한다.")
    void update_Success() {
        // given
        String newName = "new board name";
        String newDescription = "new board description";
        String newTopCategory = "new top category";
        String newSubCategory = "new sub category";
        Long updatedBy = 2L;

        // when
        board.update(newName, newDescription, newTopCategory, newSubCategory, updatedBy);

        // then
        assertEquals(newName, board.getName());
        assertEquals(newDescription, board.getDescription());
        assertEquals(newTopCategory, board.getTopCategory());
        assertEquals(newSubCategory, board.getSubCategory());
        assertEquals(updatedBy, board.getUpdatedBy());
    }

    @Test
    @DisplayName("update 는 정보를 일부만 변경할 수 있다.")
    void update_partial_Success() {
        // given
        String newName = "new board name";
        String newDescription = "new board description";
        String newTopCategory = "new top category";
        String newSubCategory = "new sub category";
        Long updatedBy = 2L;

        // when
        board.update(null, null, null, null, updatedBy);
        // then
        assertEquals(name, board.getName());
        assertEquals(description, board.getDescription());
        assertEquals(topCategory, board.getTopCategory());
        assertEquals(subCategory, board.getSubCategory());
        assertEquals(updatedBy, board.getUpdatedBy());

        // when
        board.update(newName, null, null, null, updatedBy);
        // then
        assertEquals(newName, board.getName());

        // when
        board.update(null, newDescription, null, null, updatedBy);
        // then
        assertEquals(newDescription, board.getDescription());

        // when
        board.update(null, null, newTopCategory, null, updatedBy);
        // then
        assertEquals(newTopCategory, board.getTopCategory());

        // when
        board.update(null, null, null, newSubCategory, updatedBy);
        // then
        assertEquals(newSubCategory, board.getSubCategory());
    }
}
