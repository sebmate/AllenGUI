public class Statement {

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getPredicate() {
		return predicate;
	}

	public void setPredicate(String predicate) {
		this.predicate = predicate;
	}

	public String getObject() {
		return object;
	}

	public void setObject(String object) {
		this.object = object;
	}

	private String subject = "";
	private String predicate = "";
	private String object = "";
	private String comment = "";

	public Statement(String subject, String predicate, String object) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
	}

	public Statement(String subject, String predicate, String object, String comment) {
		this.subject = subject;
		this.predicate = predicate;
		this.object = object;
		this.comment = comment;
	}

	public void print(boolean printComment, boolean filterOutLinks) {

		if (filterOutLinks && (this.subject.startsWith("#") || this.object.startsWith("#")))
			return;

		if (printComment) {
			System.out.print(this.subject + " " + this.predicate + " " + this.object);
			if (!this.comment.equals(""))
				System.out.print("       since " + this.comment);
			System.out.println("");
		} else {
			System.out.print(this.subject + " " + this.predicate + " " + this.object);
			System.out.println("");
		}
	}

	public void printWithComment() {

	}

	public String toString() {
		return this.subject + " " + this.predicate + " " + this.object;
	}

}
