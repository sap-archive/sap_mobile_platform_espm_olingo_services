package com.xsmp.espm.model;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NoResultException;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.PrimaryKeyJoinColumn;
import javax.persistence.Query;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.TypedQuery;
import javax.persistence.CascadeType;

import com.xsmp.espm.util.Utility;

import org.apache.olingo.odata2.api.commons.HttpStatusCodes;
import org.apache.olingo.odata2.api.exception.ODataRuntimeApplicationException;

@Entity
@Table(name = "ESPM_PRODUCT")
@EntityListeners(com.xsmp.deltasupport.ESPMJPADeltaListener.class)
public class Product {

	transient private static Map<String, BigDecimal> prices = null;

	@Id
	@Column(name = "PRODUCT_ID", length = 10)
	private String productId;

	private String name;

	@Column(name = "SHORT_DESCRIPTION")
	private String shortDescription;

	@Column(name = "LONG_DESCRIPTION")
	private String longDescription;

	@Column(length = 40)
	private String category;

	@Column(name = "CATEGORY_NAME", length = 40)
	private String categoryName;

	@Column(name = "QUANTITY_UNIT", length = 3)
	private String quantityUnit;

	@Column(name = "WEIGHT", precision = 13, scale = 3)
	private BigDecimal weight;

	@Column(name = "WEIGHT_UNIT", length = 3)
	private String weightUnit;

	@Column(precision = 23, scale = 3)
	private BigDecimal price;

	@Column(name = "CURRENCY_CODE", length = 5)
	private String currencyCode = "EUR";

	@Column(name = "DIMENSION_WIDTH", precision = 13, scale = 4)
	private BigDecimal dimensionWidth;

	@Column(name = "DIMENSION_DEPTH", precision = 13, scale = 4)
	private BigDecimal dimensionDepth;

	@Column(name = "DIMENSION_HEIGHT", precision = 13, scale = 4)
	private BigDecimal dimensionHeight;

	@Column(name = "DIMENSION_UNIT", length = 3)
	private String dimensionUnit;

	@Column(name = "PICTURE_URL")
	private String pictureUrl;

	@Column(name = "SUPPLIER_ID", length = 10)
	private String supplierId;

	@ManyToOne(fetch=FetchType.EAGER)
	private Supplier supplier;
	
	@OneToOne(cascade=CascadeType.ALL, fetch=FetchType.EAGER)
	@PrimaryKeyJoinColumn
	Stock stock;
	
	@Basic(optional = false)
	@Temporal(TemporalType.TIMESTAMP)
	protected Calendar updatedTimestamp;

	public String getProductId() {
		return this.productId;
	}

	public void setProductId(String param) {
		this.productId = param;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getShortDescription() {
		return this.shortDescription;
	}

	public void setShortDescription(String description) {
		this.shortDescription = description;
	}

	public String getLongDescription() {
		return this.longDescription;
	}

	public void setLongDescription(String description) {
		this.longDescription = description;
	}

	public String getCategory() {
		return this.category;
	}

	public void setCategory(String param) {
		this.category = param;
	}

	public String getCategoryName() {
		return categoryName;
	}

	public void setCategoryName(String categoryName) {
		this.categoryName = categoryName;
	}

	public String getQuantityUnit() {
		return this.quantityUnit;
	}

	public void setQuantityUnit(String param) {
		this.quantityUnit = param;
	}

	public BigDecimal getWeight() {
		return this.weight;
	}

	public void setWeight(BigDecimal param) {
		this.weight = param;
	}

	public String getWeightUnit() {
		return weightUnit;
	}

	public void setWeightUnit(String param) {
		this.weightUnit = param;
	}

	public BigDecimal getPrice() {
		return this.price;
	}

	public void setPrice(BigDecimal param) {
		this.price = param;
	}

	public String getCurrencyCode() {
		return currencyCode;
	}

	public void setCurrencyCode(String param) {
		this.currencyCode = param;
	}

	public BigDecimal getDimensionWidth() {
		return this.dimensionWidth;
	}

	public void setDimensionWidth(BigDecimal param) {
		this.dimensionWidth = param;
	}

	public BigDecimal getDimensionDepth() {
		return this.dimensionDepth;
	}

	public void setDimensionDepth(BigDecimal param) {
		this.dimensionDepth = param;
	}

	public BigDecimal getDimensionHeight() {
		return this.dimensionHeight;
	}

	public void setDimensionHeight(BigDecimal param) {
		this.dimensionHeight = param;
	}

	public String getDimensionUnit() {
		return this.dimensionUnit;
	}

	public void setDimensionUnit(String param) {
		this.dimensionUnit = param;
	}

	public String getPictureUrl() {
		return this.pictureUrl;
	}

	public void setPictureUrl(String param) {
		this.pictureUrl = param;
	}

	public void setSupplierId(String param) {
		this.supplierId = param;
	}

	public String getSupplierId() {
		return supplierId;
	}

	public Supplier getSupplier() {
		return supplier;
	}

	public void setSupplier(Supplier param) {
		this.supplier = param;
	}

	static BigDecimal getPrice(String productId) {
		if (prices == null) {
			updatePrices();
		}
		return prices.get(productId);
	}

	private static void updatePrices() {
		EntityManagerFactory emf = Utility.getEntityManagerFactory();
		EntityManager em = emf.createEntityManager();
		try {
			List<Product> products = em.createQuery("SELECT p FROM Product p",
					Product.class).getResultList();
			prices = new HashMap<String, BigDecimal>();
			for (Product p : products) {
				prices.put(p.getProductId(), p.getPrice());
			}
		} finally {
			em.close();
		}
	}

	private static void invalidatePrices() {
		if (prices != null) {
			prices.clear();
		}
		prices = null;
	}

	@PostLoad
	private void postLoad() {
		EntityManagerFactory emf = Utility.getEntityManagerFactory();
		EntityManager em = emf.createEntityManager();
		try {
			ProductText productText = getProductText(em);
			if (productText != null) {
				this.name = productText.getName();
				this.shortDescription = productText.getShortDescription();
				this.longDescription = productText.getLongDescription();
			} else {
				this.name = "";
				this.shortDescription = "";
				this.longDescription = "";
			}
		} finally {
			em.close();
		}
	}

	@PrePersist
	@PreUpdate
	private void persist() throws ODataRuntimeApplicationException {
		EntityManagerFactory emf = Utility.getEntityManagerFactory();
		EntityManager em = emf.createEntityManager();
		Calendar calendar = Calendar.getInstance();
		setUpdatedTimestamp( calendar );
		
		validateFieldsBeforeSave();
		
		try {
			em.getTransaction().begin();
			ProductText productText = getProductText(em);
			if (productText == null) {
				// INSERT
				productText = new ProductText();
				productText.setProductId(this.getProductId());
				productText.setLanguage("EN");
				productText.setName(this.name);
				productText.setShortDescription(this.shortDescription);
				productText.setLongDescription(this.longDescription);
				em.persist(productText);
			} else {
				// UPDATE
				productText.setName(this.name);
				productText.setShortDescription(this.shortDescription);
				productText.setLongDescription(this.longDescription);
			}

			em.getTransaction().commit();
		} finally {
			// ProductCategory.invalidateNumbers();
			invalidatePrices();
			em.close();
		}
	}

	private ProductText getProductText(EntityManager em) {
		TypedQuery<ProductText> query = em
				.createQuery(
						"SELECT p FROM ProductText p WHERE p.productId = :productId AND p.language = :language",
						ProductText.class);
		try {
			return query.setParameter("productId", this.getProductId())
					.setParameter("language", "EN").getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public Calendar getUpdatedTimestamp() {
		return updatedTimestamp;
	}

	public void setUpdatedTimestamp(Calendar updatedTimestamp) {
		this.updatedTimestamp = updatedTimestamp;
	}

	public static Map<String, BigDecimal> getPrices() {
		return prices;
	}

	public static void setPrices(Map<String, BigDecimal> prices) {
		Product.prices = prices;
	}

	public Stock getStock() {
		return stock;
	}

	public void setStock(Stock stock) {
		this.stock = stock;
	}
	
	private void validateFieldsBeforeSave() throws ODataRuntimeApplicationException {
		// reject null or empty Name field
		if (name == null || name.length() == 0) {
			throw new ODataRuntimeApplicationException(
						"Invalid value for name field", 
						Locale.ROOT, 
						HttpStatusCodes.BAD_REQUEST
						);
		}
	}
}