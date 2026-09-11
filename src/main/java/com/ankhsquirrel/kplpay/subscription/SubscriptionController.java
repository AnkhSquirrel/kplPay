package com.ankhsquirrel.kplpay.subscription;

import com.ankhsquirrel.kplpay.common.ApiError;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Tag(name = "Subscriptions", description = "Recurring billing subscriptions for a customer")
@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {

    private final SubscriptionService service;

    public SubscriptionController(SubscriptionService service) {
        this.service = service;
    }

    @Operation(summary = "Create a subscription",
            description = "Creates a recurring subscription for an existing customer.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Subscription created",
                    content = @Content(schema = @Schema(implementation = SubscriptionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Request validation failed or the body is malformed",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "customerId does not reference an existing customer",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
    })
    @PostMapping
    public ResponseEntity<SubscriptionResponse> create(@Valid @RequestBody CreateSubscriptionRequest request) {
        SubscriptionResponse body = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(body.id())
                .toUri();
        return ResponseEntity.created(location).body(body);
    }
}
