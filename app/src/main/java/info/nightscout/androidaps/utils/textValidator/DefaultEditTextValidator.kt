package info.nightscout.androidaps.utils.textValidator

import android.content.Context
import android.content.res.TypedArray
import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout
import info.nightscout.androidaps.R
import info.nightscout.androidaps.utils.textValidator.validators.*

class DefaultEditTextValidator : EditTextValidator {
    protected var mValidator: MultiValidator? = null
    protected var testErrorString: String? = null
    protected var emptyAllowed = false
    protected lateinit var editTextView: EditText
    protected var testType: Int
    protected var classType: String? = null
    protected var customRegexp: String? = null
    protected var customFormat: String? = null
    protected var emptyErrorStringActual: String? = null
    protected var emptyErrorStringDef: String? = null
    protected var minLength = 0
    protected var minNumber = 0
    protected var maxNumber = 0
    protected var floatminNumber = 0f
    protected var floatmaxNumber = 0f
    private var tw: TextWatcher? = null
    private var defaultEmptyErrorString: String? = null

    @Suppress("unused")
    constructor(editTextView: EditText, context: Context) {
        testType = EditTextValidator.TEST_NOCHECK
        setEditText(editTextView)
        resetValidators(context)
    }

    constructor(editTextView: EditText, typedArray: TypedArray, context: Context) {
        emptyAllowed = typedArray.getBoolean(R.styleable.FormEditText_emptyAllowed, false)
        testType = typedArray.getInt(R.styleable.FormEditText_testType, EditTextValidator.TEST_NOCHECK)
        testErrorString = typedArray.getString(R.styleable.FormEditText_testErrorString)
        classType = typedArray.getString(R.styleable.FormEditText_classType)
        customRegexp = typedArray.getString(R.styleable.FormEditText_customRegexp)
        emptyErrorStringDef = typedArray.getString(R.styleable.FormEditText_emptyErrorString)
        customFormat = typedArray.getString(R.styleable.FormEditText_customFormat)
        if (testType == EditTextValidator.TEST_MIN_LENGTH)
            minLength = typedArray.getInt(R.styleable.FormEditText_minLength, 0)
        if (testType == EditTextValidator.TEST_NUMERIC_RANGE) {
            minNumber = typedArray.getInt(R.styleable.FormEditText_minNumber, Int.MIN_VALUE)
            maxNumber = typedArray.getInt(R.styleable.FormEditText_maxNumber, Int.MAX_VALUE)
        }
        if (testType == EditTextValidator.TEST_FLOAT_NUMERIC_RANGE) {
            floatminNumber = typedArray.getFloat(R.styleable.FormEditText_floatminNumber, Float.MIN_VALUE)
            floatmaxNumber = typedArray.getFloat(R.styleable.FormEditText_floatmaxNumber, Float.MAX_VALUE)
        }
        typedArray.recycle()
        setEditText(editTextView)
        resetValidators(context)
    }

    @Throws(IllegalArgumentException::class)
    override fun addValidator(theValidator: Validator) {
        requireNotNull(theValidator) { "theValidator argument should not be null" }
        mValidator!!.enqueue(theValidator)
    }

    private fun setEditText(editText: EditText) {
        //editTextView?.removeTextChangedListener(textWatcher)
        editTextView = editText
        editText.addTextChangedListener(textWatcher)
    }

    override fun getTextWatcher(): TextWatcher {
        if (tw == null) {
            tw = object : TextWatcher {
                override fun afterTextChanged(s: Editable) {
                    testValidity()
                }

                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    if (!TextUtils.isEmpty(s) && isErrorShown) {
                        try {
                            val textInputLayout = editTextView.parent as TextInputLayout
                            textInputLayout.isErrorEnabled = false
                        } catch (e: Throwable) {
                            editTextView.error = null
                        }
                    }
                }
            }
        }
        return tw!!
    }

    override fun isEmptyAllowed(): Boolean {
        return emptyAllowed
    }

    override fun resetValidators(context: Context) {
        // its possible the context may have changed so re-get the defaultEmptyErrorString
        defaultEmptyErrorString = context.getString(R.string.error_field_must_not_be_empty)
        setEmptyErrorString(emptyErrorStringDef)
        mValidator = AndValidator()
        val toAdd: Validator
        when (testType) {
            EditTextValidator.TEST_NOCHECK             -> toAdd = DummyValidator()
            EditTextValidator.TEST_ALPHA               -> toAdd = AlphaValidator(if (TextUtils.isEmpty(testErrorString)) context.getString(R.string.error_only_standard_letters_are_allowed) else testErrorString)
            EditTextValidator.TEST_ALPHANUMERIC        -> toAdd = AlphaNumericValidator(
                if (TextUtils.isEmpty(testErrorString)) context.getString(R.string.error_this_field_cannot_contain_special_character) else testErrorString)
            EditTextValidator.TEST_NUMERIC             -> toAdd = NumericValidator(if (TextUtils.isEmpty(testErrorString)) context.getString(R.string.error_only_numeric_digits_allowed) else testErrorString)
            EditTextValidator.TEST_NUMERIC_RANGE       -> toAdd = NumericRangeValidator(if (TextUtils.isEmpty(testErrorString)) context.getString(R.string.error_only_numeric_digits_range_allowed, Integer.toString(minNumber), Integer.toString(maxNumber)) else testErrorString, minNumber, maxNumber)
            EditTextValidator.TEST_FLOAT_NUMERIC_RANGE -> toAdd = FloatNumericRangeValidator(if (TextUtils.isEmpty(testErrorString)) context.getString(R.string.error_only_numeric_digits_range_allowed, java.lang.Float.toString(floatminNumber), java.lang.Float.toString(floatmaxNumber)) else testErrorString, floatminNumber, floatmaxNumber)
            EditTextValidator.TEST_REGEXP              -> toAdd = RegexpValidator(testErrorString, customRegexp)
            EditTextValidator.TEST_CREDITCARD          -> toAdd = CreditCardValidator(if (TextUtils.isEmpty(testErrorString)) context.getString(R.string.error_creditcard_number_not_valid) else testErrorString)
            EditTextValidator.TEST_EMAIL               -> toAdd = EmailValidator(if (TextUtils.isEmpty(testErrorString)) context.getString(R.string.error_email_address_not_valid) else testErrorString)
            EditTextValidator.TEST_PHONE               -> toAdd = PhoneValidator(if (TextUtils.isEmpty(testErrorString)) context.getString(R.string.error_phone_not_valid) else testErrorString)
            EditTextValidator.TEST_MULTI_PHONE         -> toAdd = MultiPhoneValidator(if (TextUtils.isEmpty(testErrorString)) context.getString(R.string.error_phone_not_valid) else testErrorString)
            EditTextValidator.TEST_PIN_STRENGTH        -> toAdd = PinStrengthValidator(if (TextUtils.isEmpty(testErrorString)) context.getString(R.string.error_pin_not_valid) else testErrorString)
            EditTextValidator.TEST_DOMAINNAME          -> toAdd = DomainValidator(if (TextUtils.isEmpty(testErrorString)) context.getString(R.string.error_domain_not_valid) else testErrorString)
            EditTextValidator.TEST_IPADDRESS           -> toAdd = IpAddressValidator(if (TextUtils.isEmpty(testErrorString)) context.getString(R.string.error_ip_not_valid) else testErrorString)
            EditTextValidator.TEST_WEBURL              -> toAdd = WebUrlValidator(if (TextUtils.isEmpty(testErrorString)) context.getString(R.string.error_url_not_valid) else testErrorString)
            EditTextValidator.TEST_HTTPS_URL           -> toAdd = HttpsUrlValidator(if (TextUtils.isEmpty(testErrorString)) context.getString(R.string.error_url_not_valid) else testErrorString)
            EditTextValidator.TEST_PERSONNAME          -> toAdd = PersonNameValidator(if (TextUtils.isEmpty(testErrorString)) context.getString(R.string.error_notvalid_personname) else testErrorString)
            EditTextValidator.TEST_PERSONFULLNAME      -> toAdd = PersonFullNameValidator(if (TextUtils.isEmpty(testErrorString)) context.getString(R.string.error_notvalid_personfullname) else testErrorString)
            EditTextValidator.TEST_MIN_LENGTH          -> toAdd = MinDigitLengthValidator(if (TextUtils.isEmpty(testErrorString)) context.getString(R.string.error_not_a_minimum_length) else testErrorString, minLength)

            EditTextValidator.TEST_CUSTOM              -> {
                // must specify the fully qualified class name & an error message
                if (classType == null)
                    throw RuntimeException("Trying to create a custom validator but no classType has been specified.")
                if (TextUtils.isEmpty(testErrorString))
                    throw RuntimeException(String.format("Trying to create a custom validator (%s) but no error string specified.", classType))

                val customValidatorClass: Class<out Validator> = try {
                    this.javaClass.classLoader?.loadClass(classType)?.let {
                        if (!Validator::class.java.isAssignableFrom(it)) {
                            throw RuntimeException(String.format("Custom validator (%s) does not extend %s", classType, Validator::class.java.name))
                        }
                        @Suppress("Unchecked_Cast")
                        it as Class<out Validator>
                    }!!
                } catch (e: ClassNotFoundException) {
                    throw RuntimeException(String.format("Unable to load class for custom validator (%s).", classType))
                }
                toAdd = try {
                    customValidatorClass.getConstructor(String::class.java).newInstance(testErrorString)
                } catch (e: Exception) {
                    throw RuntimeException(String.format("Unable to construct custom validator (%s) with argument: %s", classType,
                        testErrorString))
                }
            }

            EditTextValidator.TEST_DATE                -> toAdd = DateValidator(if (TextUtils.isEmpty(testErrorString)) context.getString(R.string.error_date_not_valid) else testErrorString, customFormat)
            else                                       -> toAdd = DummyValidator()
        }
        val tmpValidator: MultiValidator
        if (!emptyAllowed) { // If the xml tells us that this is a required field, we will add the EmptyValidator.
            tmpValidator = AndValidator()
            tmpValidator.enqueue(EmptyValidator(emptyErrorStringActual))
            tmpValidator.enqueue(toAdd)
        } else {
            tmpValidator = OrValidator(toAdd.errorMessage, NotValidator(null, EmptyValidator(null)), toAdd)
        }
        addValidator(tmpValidator)
    }

    @Suppress("unused")
    fun setClassType(classType: String?, testErrorString: String?, context: Context) {
        testType = EditTextValidator.TEST_CUSTOM
        this.classType = classType
        this.testErrorString = testErrorString
        resetValidators(context)
    }

    @Suppress("unused")
    fun setCustomRegexp(customRegexp: String?, context: Context) {
        testType = EditTextValidator.TEST_REGEXP
        this.customRegexp = customRegexp
        resetValidators(context)
    }

    @Suppress("unused")
    fun setEmptyAllowed(emptyAllowed: Boolean, context: Context) {
        this.emptyAllowed = emptyAllowed
        resetValidators(context)
    }

    fun setEmptyErrorString(emptyErrorString: String?) {
        emptyErrorStringActual = if (!TextUtils.isEmpty(emptyErrorString)) {
            emptyErrorString
        } else {
            defaultEmptyErrorString
        }
    }

    @Suppress("unused")
    fun setTestErrorString(testErrorString: String?, context: Context) {
        this.testErrorString = testErrorString
        resetValidators(context)
    }

    @Suppress("unused")
    fun setTestType(testType: Int, context: Context) {
        this.testType = testType
        resetValidators(context)
    }

    override fun testValidity(): Boolean {
        return testValidity(true)
    }

    override fun testValidity(showUIError: Boolean): Boolean {
        val isValid = mValidator?.isValid(editTextView) ?: false
        if (!isValid && showUIError) {
            showUIError()
        }
        return isValid
    }

    override fun showUIError() {
        mValidator?.let { mValidator ->
            if (mValidator.hasErrorMessage()) {
                try {
                    val parent = editTextView.parent as TextInputLayout
                    parent.isErrorEnabled = true
                    parent.error = mValidator.errorMessage
                } catch (e: Throwable) {
                    editTextView.error = mValidator.errorMessage
                }
            }
        }
    }

    // might sound like a bug. but there's no way to know if the error is shown (not with public api)
    val isErrorShown: Boolean
        get() = try {
            editTextView.parent as TextInputLayout
            true // might sound like a bug. but there's no way to know if the error is shown (not with public api)
        } catch (e: Throwable) {
            !TextUtils.isEmpty(editTextView.error)
        }
}