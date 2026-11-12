"""Reproduce the JavaBeans from the reviewed field contracts (UTF-8)."""
from pathlib import Path

entities = {
    'Account6': ('', 'Integer id; String username; String password; String profileImage; String status'),
    'Member6': ('', 'Integer id; String fullName; String phone; String email; String address; Account6 acc'),
    'Customer6': ('Member6', 'String customerCode'),
    'Employee6': ('Member6', 'String employeeCode; String jobTitle'),
    'Manager6': ('Employee6', ''),
    'SaleStaff6': ('Employee6', ''),
    'Technician6': ('Employee6', ''),
    'VehicleBrand6': ('', 'Integer id; String name'),
    'PartBrand6': ('', 'Integer id; String name'),
    'Category6': ('', 'Integer id; String categoryName'),
    'Vehicle6': ('', 'Integer id; String licensePlate; String vehicleModel; Integer currentMileage; Integer manufactureYear; Customer6 cus; VehicleBrand6 brand'),
    'AppointmentSlot6': ('', 'Integer id; Date startTime; String holdCode; Date holdExpiresAt; String status'),
    'Appointment6': ('', 'Integer id; String appointmentCode; String requestDescription; Date createAt; String status; SaleStaff6 ss; Vehicle6 vehicle; AppointmentSlot6 slot; Customer6 cus'),
    'RevenueStats6': ('', 'String itemCode; String itemName; String itemType; Integer usageCount; BigDecimal totalRevenue'),
    'Service6': ('', 'Integer id; String serviceCode; String serviceName; String description; BigDecimal unitPrice'),
    'SparePart6': ('', 'Integer id; String partCode; String partName; String description; String unit; String technicalSpecifications; BigDecimal unitPrice; Integer stockQuantity; String image; PartBrand6 brand; Category6 cat'),
    'ServiceUsage6': ('', 'Integer id; Date usageTime; BigDecimal saleUnitPrice; String note; Service6 service; Technician6 tech; RepairOrder6 order'),
    'PartUsage6': ('', 'Integer id; Integer usedQuantity; Date usageTime; BigDecimal saleUnitPrice; String note; SparePart6 sparePart; Technician6 tech; RepairOrder6 order'),
    'RepairOrder6': ('', 'Integer id; String repairOrderCode; Date createTime; Date completionTime; String vehicleCondition; String repairDescription; String repairResult; String repairStatus; ServiceUsage6[] serviceUsage; PartUsage6[] partUsage; Vehicle6 vehicle; Customer6 cus; SaleStaff6 ss; Appointment6 appointment'),
    'Payment6': ('', 'Integer id; BigDecimal paymentAmount; Date paymentTime; String paymentMethod; String status; SaleStaff6 ss; String transactionReference'),
    'Invoice6': ('', 'Integer id; String invoiceCode; BigDecimal totalAmount; Date issueDate; RepairOrder6 order; Payment6 payment'),
}
root = Path('src/main/java/garaman/entity')
root.mkdir(parents=True, exist_ok=True)
for name, (parent, fields) in entities.items():
    rows = [field.strip().split() for field in fields.split(';') if field.strip()]
    imports = '\nimport java.util.Date;\n' if any(t == 'Date' for t, n in rows) else ''
    imports += '\nimport java.math.BigDecimal;\nimport java.math.RoundingMode;\n' if any(t == 'BigDecimal' for t, n in rows) else ''
    imports += '\nimport java.util.Locale;\n' if name == 'Vehicle6' else ''
    source = f'package garaman.entity;\n{imports}\npublic class {name}' + (f' extends {parent}' if parent else '') + ' {\n'
    for typ, field in rows:
        default = ' = new ' + typ[:-2] + '[0]' if typ.endswith('[]') else ''
        source += f'    private {typ} {field}{default};\n'
    source += f'\n    public {name}() {{}}\n'
    for typ, field in rows:
        cap = field[0].upper() + field[1:]
        source += f'    public {typ} get{cap}() {{ return {field}; }}\n'
        value = f'{field} == null ? null : {field}.setScale(2, RoundingMode.UNNECESSARY)' if typ == 'BigDecimal' else field
        source += f'    public void set{cap}({typ} {field}) {{ this.{field} = {value}; }}\n'
    if name == 'Vehicle6':source += '    public String getVehicleCode() { return id == null || id <= 0 ? null : "X" + String.format(Locale.ROOT, "%04d", id); }\n'
    if name == 'PartUsage6':source += '    public BigDecimal getLineAmount() { return saleUnitPrice.multiply(BigDecimal.valueOf(usedQuantity)); }\n'
    source += '}\n'
    (root / f'{name}.java').write_text(source, encoding='utf-8')
print(f'Generated {len(entities)} JavaBeans from reviewed contracts.')
