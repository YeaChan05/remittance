package org.yechan.remittance.transfer;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import org.yechan.remittance.transfer.dto.TransferQueryResponse;
import org.yechan.remittance.transfer.dto.TransferRequest;

@Tag(name = "Transfer", description = "송금 API")
interface TransferApi {

  @Operation(summary = "송금 요청", description = "멱등키로 송금을 요청합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "송금 성공", content = @Content)
  })
  TransferResult transfer(
      @Parameter(hidden = true) Long memberId,
      @Parameter(description = "멱등키") String idempotencyKey,
      TransferRequest props
  );

  @Operation(summary = "거래 내역 조회", description = "계좌 기준 거래 내역을 조회합니다.")
  @ApiResponses({
      @ApiResponse(responseCode = "200", description = "조회 성공", content = @Content)
  })
  TransferQueryResponse query(
      @Parameter(hidden = true) Long memberId,
      @Parameter(description = "계좌 ID") Long accountId,
      @Parameter(description = "조회 시작(UTC)") LocalDateTime from,
      @Parameter(description = "조회 종료(UTC)") LocalDateTime to,
      @Parameter(description = "최대 개수") Integer limit
  );
}
