package pan.affiliation.application.usecases.customers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pan.affiliation.domain.modules.customers.commands.ChangeCustomerCommandHandler;
import pan.affiliation.domain.modules.customers.entities.Address;
import pan.affiliation.domain.modules.customers.entities.Customer;
import pan.affiliation.domain.modules.customers.queries.GetCustomerByIdQueryHandler;
import pan.affiliation.shared.validation.ValidationContext;
import pan.affiliation.shared.validation.ValidationStatus;

import static pan.affiliation.shared.constants.Messages.NOT_FOUND_RECORD;

@Service
public class SaveAddressUseCase extends ChangeCustomerBaseUseCase {
    @Autowired
    public SaveAddressUseCase(
            ChangeCustomerCommandHandler command,
            ValidationContext validationContext,
            GetCustomerByIdQueryHandler query) {
        super(command, validationContext, query);
    }

    public Address saveAddress(SaveAddressInput input) {
        var customer = this.getCustomerById(input.customerId());

        if (customer == null)
            return null;

        var isNewAddress = input.addressId() == null;
        var newAddressData = input.toDomainEntity();

        if (isNewAddress) {
            customer.addAddress(newAddressData);
        } else {
            if (!saveAddress(customer, newAddressData))
                return null;
        }

        var customerChanged = this.changeCustomer(customer) != null;

        if (!customerChanged)
            return null;

        return newAddressData;
    }

    private boolean saveAddress(Customer customer, Address newAddressData) {
        var addressChanged = customer.changeAddress(newAddressData);

        if(!addressChanged) {
            this.validationContext.setStatus(ValidationStatus.NOT_FOUND);
            this.validationContext.addNotification(
                    "address",
                    NOT_FOUND_RECORD
            );

            return false;
        }

        return true;
    }

}
