create table widget (
	id uuid not null default random_uuid() primary key,
	width INTEGER not null,
    height INTEGER not null,
    x_axis INTEGER not null,
    y_axis INTEGER not null,
    z_index INTEGER not null
);
