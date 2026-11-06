package until.the.eternity.dcs.domain.outfit.catalog;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import until.the.eternity.dcs.common.exception.CustomException;
import until.the.eternity.dcs.common.exception.ExceptionCode;

public class OutfitCatalogException extends CustomException {
    public OutfitCatalogException(Code code) {
        super(code);
    }

    @Getter
    @RequiredArgsConstructor
    public enum Code implements ExceptionCode {
        OUTFIT_INVALID_ITEM(HttpStatus.BAD_REQUEST, "착용 부위에 맞는 아이템을 자동완성에서 선택해 주세요."),
        OUTFIT_CATALOG_UNAVAILABLE(
                HttpStatus.SERVICE_UNAVAILABLE, "아이템 목록을 확인할 수 없습니다. 잠시 후 다시 시도해 주세요.");

        private final HttpStatus status;
        private final String message;

        @Override
        public String getCode() {
            return name();
        }
    }
}
