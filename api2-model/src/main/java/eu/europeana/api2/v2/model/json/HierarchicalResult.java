package eu.europeana.api2.v2.model.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.europeana.api2.model.json.abstracts.ApiResponse;
import eu.europeana.corelib.neo4j.entity.Neo4jBean;

import java.util.List;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

/**
 * @author Peter Kiraly <peter.kiraly@europeana.eu>
 * @author luthien
 */
@JsonInclude(NON_EMPTY)
public class HierarchicalResult extends ApiResponse {

	public HierarchicalResult() {}

	@Deprecated
	public HierarchicalResult(String apikey) {
		super(apikey);
	}

	@Deprecated
	public HierarchicalResult(String apikey, long requestNumber) {
		this(apikey);
		this.requestNumber = requestNumber;
	}

	public Neo4jBean self;

	public Neo4jBean parent;

	public List<Neo4jBean> children;

	public List<Neo4jBean> ancestors;

	@JsonProperty("preceding-siblings")
	public List<Neo4jBean> precedingSiblings;

	@JsonProperty("following-siblings")
	public List<Neo4jBean> followingSiblings;

	@JsonProperty("preceding-sibling-children")
	public List<Neo4jBean> precedingSiblingChildren;

	@JsonProperty("following-sibling-children")
	public List<Neo4jBean> followingSiblingChildren;

	public String message;
}
