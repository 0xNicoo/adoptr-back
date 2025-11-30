package com.adoptr.adoptr.auth.service.validator;

import com.adoptr.adoptr.auth.util.AuthUser;

public interface Validator {
    AuthUser validate(String idToken);
}
