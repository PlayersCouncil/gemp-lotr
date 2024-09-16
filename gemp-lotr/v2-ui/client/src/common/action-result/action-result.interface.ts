type SuccessResult<T> = {
  result?: T;
};

type ErrorResult<L> = {
  error?: L;
};

export type ActionResult<
  TSuccess = boolean,
  TError = string
> = SuccessResult<TSuccess> &
  ErrorResult<TError> & {
    success: boolean;
  };

// note to self... this pattern is a great idea, but it's just making me sad
// and quite frankly i haven't cared enough to make it right. maybe i'll just
// give it up
