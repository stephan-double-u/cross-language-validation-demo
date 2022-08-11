import {
    setValidationRules, isPropertyMandatory, isPropertyImmutable, getAllowedPropertyValues,
    validateMandatoryRules, validateContentRules, validateImmutableRules, validateUpdateRules
} from './CrossLanguageValidation_ES6.js';

function resetForm() {
    adjustFormMandatory(newArticle);
    adjustFormImmutable(newArticle);
    adjustSelectBoxes(newArticle);
    toForm(newArticle);
    toModel(savedArticle);
    toggleAnimalUseImg();
    createButton.disabled = false;
    updateButton.disabled = true;
    changeLastModifiedOnButton.disabled = true;
    validate();
}

function adjustFormMandatory(article) {
    propertiesToCheck.forEach(propName => {
        const isMandatory = isPropertyMandatory("article", propName, article, userPerms);
        console.debug("%s is mandatory: %s", propName, isMandatory);
        document.querySelector('#' + propName + 'Label').className = "mandatory-" + isMandatory;
    });
}

function adjustFormImmutable(article) {
    propertiesToCheck.forEach(propName => {
        const isImmutable = isPropertyImmutable("article", propName, article, userPerms);
        console.debug("%s is immutable: %s", propName, isImmutable);
        document.querySelector('#' + propName).disabled = isImmutable;
        if (propName === 'accessories') {
            addAccessoryButton.disabled = isImmutable;
        }
    });
}

function adjustSelectBoxes(article) {
    adjustStatusBox(article);
    adjustCategoryBox(article);
    toModel(editedArticle);
}

function adjustStatusBox(article) {
    const allowedStatus = getAllowedPropertyValues("article", "status", article, userPerms);
    console.debug("Allowed status: %s", allowedStatus);
    if (allowedStatus === undefined) {
        return;
    }
    const statusBox = document.querySelector('#status');
    const selectedStatus = statusBox.value;
    statusBox.innerHTML = "";
    for (const status of allowedStatus) {
        const selected = status === selectedStatus;
        statusBox.options.add(new Option(status, status, selected, selected));
    }
}

function adjustCategoryBox(article) {
    const allowedCategories = getAllowedPropertyValues("article", "category", article, userPerms);
    console.debug("Allowed categories: %s", allowedCategories);
    if (allowedCategories === undefined) {
        return;
    }
    const categoryBox = document.querySelector('#category');
    const selectedCategory = categoryBox.value;
    categoryBox.innerHTML = allowedCategories.includes(null) ? "<option></option>" : "";
    for (const category of allowedCategories) {
        if (category !== null) {
            const optionText = categoryMapping.category[category].label;
            const selected = category === selectedCategory;
            categoryBox.options.add(new Option(optionText, category, selected, selected));
        }
    }
    adjustSubCategoryBox(selectedCategory);
}

function adjustSubCategoryBox(selectedCategory) {
    let allowedSubCategories = [null];
    if (selectedCategory) {
        allowedSubCategories.push(...categoryMapping.category[selectedCategory].subCategories);
    }
    console.debug("Allowed subCategories: ", allowedSubCategories);
    const subCategoryBox = document.querySelector('#subCategory');
    const selectedSubCategory = subCategoryBox.value;
    subCategoryBox.innerHTML = "<option></option>";
    for (const subCategory of allowedSubCategories) {
        if (subCategory !== null) {
            const optionText = categoryMapping.subCategory[subCategory].label;
            const selected = subCategory === selectedSubCategory;
            subCategoryBox.options.add(new Option(optionText, subCategory, selected, selected));
        }
    }
}

function validate() {
    const forUpdate = !updateButton.disabled;
    console.debug("validate for %s with user perms %s", forUpdate,  JSON.stringify(userPerms));
    toModel(editedArticle);
    adjustFormMandatory(editedArticle);
    adjustSelectBoxes(savedArticle);

    const errors = validateMandatoryRules("article", editedArticle, userPerms);
    errors.push(...validateContentRules("article", editedArticle, userPerms));
    if (forUpdate) {
        errors.push(...validateImmutableRules("article", savedArticle, editedArticle, userPerms));
        errors.push(...validateUpdateRules("article", savedArticle, editedArticle, userPerms));
    }
    console.debug("validation errors: %s", errors);
    showErrorMessages(errors);
}

function toForm(article) {
    console.debug("toForm: ", article);
    document.querySelector("#id").value = article.id;
    document.querySelector("#lastModifiedOn").value = article.lastModifiedOn;
    document.querySelector("#name").value = article.name;
    document.querySelector("#number").value = article.number;
    document.querySelector("#status").value = article.status;
    document.querySelector("#animalUse").checked = article.animalUse;
    document.querySelector("#everLeftWarehouse").checked = article.everLeftWarehouse;
    document.querySelector("#medicalSet").value = article.medicalSet;
    document.querySelector("#maintenanceIntervalMonth").value = article.maintenanceIntervalMonth;
    document.querySelector("#maintenanceNextDate").value = article.maintenanceNextDate;
    document.querySelector("#category").value = article.category;
    document.querySelector("#subCategory").value = article.subCategory;
    let i = 0;
    let accessoriesHtml = article.accessories.map(acc =>
        '<div>' +
        '<input id="accName'+ i +'" type="text" value="' + acc.name + '" style="width: 152px;" onfocusout="validate()"> ' +
        '<input id="accAmount'+ i +'" type="text" style="width: 176px" value="' + acc.amount + '" onfocusout="validate()"' +
        ' oninput="this.value = this.value.replace(/[^0-9.]/g, \'\').replace(/(\\..*?)\\..*/g, \'$1\');"> ' +
        '<button onclick="removeAccessory('+ i++ +');">–</button>' +
        '</div>');
    console.debug("accessoriesHtml: %s", accessoriesHtml);
    accessoriesDiv.innerHTML = accessoriesHtml.join('');
}

function toModel(article) {
    const idValue = document.querySelector("#id").value;
    article.id = idValue === "" ? null : idValue;
    article.lastModifiedOn = document.querySelector("#lastModifiedOn").value;
    const nameValue = document.querySelector("#name").value;
    article.name = nameValue === "" ? null : nameValue;
    const numberValue = document.querySelector("#number").value;
    article.number = numberValue === "" ? null : numberValue;
    const statusValue = document.querySelector("#status").value;
    article.status = statusValue === "" ? null : statusValue;
    article.animalUse = document.querySelector("#animalUse").checked;
    article.everLeftWarehouse = document.querySelector("#everLeftWarehouse").checked;
    const medicalSetValue = document.querySelector("#medicalSet").value;
    article.medicalSet = medicalSetValue === "" ? null : medicalSetValue;
    const maintenanceIntervalMonthValue = document.querySelector("#maintenanceIntervalMonth").value;
    article.maintenanceIntervalMonth = maintenanceIntervalMonthValue === "" ? null : maintenanceIntervalMonthValue;
    const maintenanceNextDateValue = document.querySelector("#maintenanceNextDate").value;
    article.maintenanceNextDate = maintenanceNextDateValue === "" ? null : maintenanceNextDateValue;
    const categoryValue = document.querySelector("#category").value;
    article.category = categoryValue === "" ? null : categoryValue;
    const subCategoryValue = document.querySelector("#subCategory").value;
    article.subCategory = subCategoryValue === "" ? null : subCategoryValue;
    const accessories = [];
    let i = 0;
    while (document.querySelector('#accName' + i)) {
        const accName = document.querySelector('#accName' + i).value;
        const accAmount = +document.querySelector('#accAmount' + i).value;
        accessories.push({name: accName, amount: accAmount});
        i++;
    }
    article.accessories = accessories;
    console.info("toModel: ", article);
}

function showErrorMessages(errors) {
    propertiesToCheck.forEach(prop => {
        const propErrCodes = errors.filter(e => e.indexOf(".article." + prop) >= 0);
        const propErrMsgs = propErrCodes.map(errCode => getErrorMessageForCode(errCode));
        if (propErrMsgs.length >= 1 && showOnlyFirstPropError) {
            document.querySelector('#' + prop + 'Err').innerHTML = propErrMsgs[0];
        } else {
            document.querySelector('#' + prop + 'Err').innerHTML = propErrMsgs.join('<br/>');
        }
    });
}

function getErrorMessageForCode(errCode) {
    const errMsg = validationErrorCodeMap[errCode];
    return (errMsg !== undefined) ? errMsg : errCode;
}

function addAccessory() {
    toModel(editedArticle);
    editedArticle.accessories.push({name: "", amount: "1"});
    console.info("addAccessory: new length %s", editedArticle.accessories.length);
    toForm(editedArticle);
    validate();
}

function removeAccessory(index) {
    toModel(editedArticle);
    editedArticle.accessories.splice(index, 1);
    console.info("removeAccessory: index %s, remaining length %s", index, editedArticle.accessories.length);
    toForm(editedArticle);
    validate();
}

function simulateConcurrentModification() {
    const timestamp = new Date().toJSON();
    savedArticle.lastModifiedOn = timestamp;
    editedArticle.lastModifiedOn = timestamp;
    toForm(editedArticle);
}
function toggleShowFirstOrAllErrorsFlag() {
    showOnlyFirstPropError = !showOnlyFirstPropError;
    validate();
}

const putPermissions = async () => {
    userPerms.length = 0;
    if (perm1.checked) userPerms.push(perm1.name);
    if (perm2.checked) userPerms.push(perm2.name);
    console.info("putPermissions: %s", JSON.stringify(userPerms));
    const response = await fetch('http://localhost:8080/user-permissions', {
        method: 'PUT',
        body: JSON.stringify(userPerms),
        headers: {
            'Content-Type': 'application/json'
        }
    });
    validate();
}

const postArticle = async () => {
    toModel(editedArticle);
    console.info("postArticle: %s", JSON.stringify(editedArticle));
    const response = await fetch('http://localhost:8080/article', {
        method: 'POST',
        body: JSON.stringify(editedArticle),
        headers: {
            'Content-Type': 'application/json'
        }
    });
    console.info("postArticle status: %s", response.status);
    const responseJson = await response.json();
    if (response.status === 200) {
        savedArticle = responseJson;
        toForm(savedArticle);
        adjustFormImmutable(savedArticle);
        validate();
        createButton.disabled = true;
        updateButton.disabled = false;
        changeLastModifiedOnButton.disabled = false;
    } else if (response.status === 400) {
        console.info("postArticle 400: validation errors: %s", responseJson);
        showErrorMessages(responseJson);
    } else {
        console.error("postArticle: should not happen: %s", response.status);
    }
}

const putArticle = async () => {
    toModel(editedArticle);
    const response = await fetch('http://localhost:8080/article', {
        method: 'PUT',
        body: JSON.stringify(editedArticle),
        headers: {
            'Content-Type': 'application/json'
        }
    });
    console.info("putArticle status: %s", response.status);
    const responseJson = await response.json();
    if (response.status === 200) {
        savedArticle = responseJson;
        toForm(savedArticle);
        adjustFormImmutable(savedArticle);
        validate();
    } else if (response.status === 400) {
        console.info("putArticle 400: validation errors: %s", responseJson);
        showErrorMessages(responseJson);
        const concurrentModErrCode = "error.validation.immutable.article.lastModifiedOn";
        if (responseJson.includes(concurrentModErrCode)) {
            alert("Concurrent Modification Exception!\n" +
                getErrorMessageForCode(concurrentModErrCode) + "\n" +
                "Reload the article (not implemented in demo app).")
        }
    } else {
        console.error("putArticle: should not happen: %s", response.status);
    }
}

const getValidationRules = async () => {
    const response = await fetch('http://localhost:8080/validation-rules');
    const rules = await response.json();
    setValidationRules(rules);
    document.querySelector('#rules').innerHTML = "Loaded rules:<br><code>" + htmlEncode(JSON.stringify(rules)) + "</code>";
    validate();
}

function htmlEncode(str) {
    return String(str).replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

const getValidationErrorCodeMap = async () => {
    const response = await fetch('http://localhost:8080/validation-error-messages');
    validationErrorCodeMap = await response.json();
    document.querySelector('#rules').innerHTML = "Error code to message mapping:<br><code>" +
        htmlEncode(JSON.stringify(validationErrorCodeMap)) + "</code>";
    validate();
}

const getCategoryMapping = async () => {
    const response = await fetch('http://localhost:8080/category-mapping');
    categoryMapping = await response.json();
}

function toggleAnimalUseImg() {
    if (animalUse.checked) {
        document.querySelector('#animalUseImg').className = "visible";
    } else {
        document.querySelector('#animalUseImg').className = "not-visible";
    }
}

const newButton = document.querySelector('#newButton');
const createButton = document.querySelector('#createButton');
const updateButton = document.querySelector('#updateButton');
const addAccessoryButton = document.querySelector('#addAccessoryButton');
const loadRulesButton = document.querySelector('#loadRulesButton');
const loadErrorMessagesButton = document.querySelector('#loadErrorMessagesButton');
const toggleShowFirstOrAllErrorsButton = document.querySelector('#toggleShowFirstOrAllErrorsButton');
const changeLastModifiedOnButton = document.querySelector('#changeLastModifiedOnButton');

newButton.addEventListener('click', resetForm);
createButton.addEventListener('click', postArticle);
updateButton.addEventListener('click', putArticle);
addAccessoryButton.addEventListener('click', addAccessory);
loadRulesButton.addEventListener('click', getValidationRules);
loadErrorMessagesButton.addEventListener('click', getValidationErrorCodeMap);
toggleShowFirstOrAllErrorsButton.addEventListener('click', toggleShowFirstOrAllErrorsFlag);
changeLastModifiedOnButton.addEventListener('click', simulateConcurrentModification);

const perm1 = document.querySelector('#perm1')
const perm2 = document.querySelector('#perm2')
const userPerms = [];

perm1.addEventListener('change', putPermissions);
perm2.addEventListener('change', putPermissions);

const animalUse = document.querySelector('#animalUse');
animalUse.addEventListener('change', toggleAnimalUseImg);

const accessoriesDiv = document.querySelector("#accessories")

const emptyArticle = '{"id":null,"lastModifiedOn":null,"name":null,"number":null,"status":null,' +
    '"animalUse":null,"everLeftWarehouse":null,"medicalSet":null,"maintenanceIntervalMonth":null,' +
    '"maintenanceNextDate":null,"category":null,"subCategory":null,"accessories":[]}';
let newArticle = JSON.parse(emptyArticle);
let savedArticle = JSON.parse(emptyArticle);
let editedArticle = JSON.parse(emptyArticle);

const propertiesToCheck = ['name', 'number', 'status', 'animalUse', 'everLeftWarehouse', 'medicalSet', 'accessories',
    'maintenanceNextDate', 'maintenanceIntervalMonth', 'category', 'subCategory']
//const propertiesToCheck = ['maintenanceNextDate', 'maintenanceIntervalMonth']
let categoryMapping = {};
let validationErrorCodeMap = {};
let showOnlyFirstPropError = false;

window.validate = validate;
window.removeAccessory = removeAccessory;

getCategoryMapping();
resetForm();
