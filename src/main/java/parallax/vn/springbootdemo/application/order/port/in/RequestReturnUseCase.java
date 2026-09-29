package parallax.vn.springbootdemo.application.order.port.in;

import parallax.vn.springbootdemo.application.order.dto.RequestReturnCommand;
import parallax.vn.springbootdemo.application.order.dto.ReturnResult;

public interface RequestReturnUseCase {
    ReturnResult requestReturn(RequestReturnCommand command);
}
