package com.sgs.MortgageApplicationService.ClientControl;

import com.sgs.MortgageApplicationService.Client.ClientRepository;
import com.sun.security.ntlm.Client;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Collections;
import java.util.Optional;

@Tag(name = "Client", description = "Client API")
@RestController
@RequestMapping(path = "/client")
public class ClientControl {
    private final ClientRepository clientRepository;

    public ClientControl(ClientRepository clientRepository){
        this.clientRepository = clientRepository;
    }

    @Operation(
            operationId = "getClient",
            summary = "Найти клиента по ID",
            description = "Return single client",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "OK",
                            content = {
                                    @Content(
                                            mediaType = "application/json",
                                            schema = @Schema(
                                                    implementation = Client.class
                                            )
                                    ),
                            }
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Not Found",
                            content = {@Content}
                    )
            }
    )
    @GetMapping("/client/{id}")
    public ResponseEntity<?> getByID(@PathVariable String id) {
        if (id.length() != 36) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("error", "invalid id"));
        }
        Optional<Client> savedClient;
        savedClient = clientRepository.findById(id);
        if (savedClient.isPresent()) {
            return ResponseEntity.of(savedClient);
        }
        return new ResponseEntity<String>(HttpStatus.NOT_FOUND);
    }
    @Operation(
            operationId = "createClient",
            summary = "Оформить заявку на ипотеку",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            content = {
                                    @Content(schema = @Schema(implementation = CustomerWithoutId.class))
                            }
                    )
            }
    )
    @PostMapping
    ResponseEntity<?> createCustomer(@RequestBody ClientWithoutId client) {
        com.sgs.MortgageApplicationService.Client.Client clientWithId = client.getClient(client);
        if (!isExpected(client)) {
            if (!clientWithId.cellNoZero()) {
                return ResponseEntity.badRequest().
                        body(Collections.singletonMap("error", "one of the fields is zero"));
            }
            MortgageCalculatorApi mortgageCalculatorApi = new MortgageCalculatorApi();
            MortgageCalculateParams calculateParams = new MortgageCalculateParams();
            calculateParams.setCreditAmount(customerWithId.getCreditAmount());
            calculateParams.setDurationInMonths(customerWithId.getDurationInMonths());
            BigDecimal monthlyPayment = mortgageCalculatorApi.calculate(calculateParams).getMonthlyPayment();
            if (!clientWithId.cellNoEmpty()) {
                return ResponseEntity.badRequest().
                        body(Collections.singletonMap("error", "one of the fields is null"));
            }
            if (client.getSalary().divide(monthlyPayment, 0, RoundingMode.DOWN).compareTo(new BigDecimal(2)) == 1) {
                clientWithId.setStatus(com.sgs.MortgageApplicationService.Client.Client.MortgageStatus.APPROVED);
                clientWithId.setMonthlyPayment(monthlyPayment);
                clientRepository.save(clientWithId);
            } else {
                clientWithId.setStatus(Status.DENIED);
                clientRepository.save(clientWithId);
            }
            return ResponseEntity.created(ServletUriComponentsBuilder.fromCurrentRequest().path("/customer/{id}").
                    build(Collections.singletonMap("id", customerWithId.getId()))).body(customerWithId);
        } else {
            return new ResponseEntity<String>(HttpStatus.CONFLICT);
        }
    }
    boolean isExpected(ClientWithoutId clientWithoutId){
        if (clientRepository.findByFirstNameAndSecondNameAndLastNameAndPassport(clientWithoutId.getFirstName(),
                clientWithoutId.getSecondName(), clientWithoutId.getLastName(),
                clientWithoutId.getPassport()) == null) {
            return false;
        } else {
            return true;

        }

}
