import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@OpenAPIDefinition(
     info = @Info(title = "Mortgage Application Service",description = "API ипотечных заявок", version = "1.0")
)

@SpringBootApplication
class MortgageApplicationServiceApp {

    public static void main(String[] args){
        SpringApplication.run(MortgageApplicationServiceApp.class, args);
    }
}
