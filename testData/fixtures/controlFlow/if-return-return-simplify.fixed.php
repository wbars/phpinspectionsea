<?php

class CasesHolder {
    public function ifReturnReturnOne($x) {
        return !($x > 0);
    }
    public function ifReturnReturnTwo($x) {
        return $x > 0;
    }
    public function ifReturnReturnThree($x) {
        if ($x === 0) { $x = 0; }
        return $x > 0;
    }
    public function ifReturnReturnFour($x) {
        if ($x === 0) { return true; }
        /* a comment here */
        if ($x > 0) { return true; }
        return false;
    }
    public function ifReturnReturnFive($x) {
        if ($x > 0) { return true; }
        return true;
    }
    public function ifReturnReturnSix($x) {
        return is_numeric($x);
    }

    public function ifReturnElseReturnOne($x) {
        return $x > 0;
    }
    public function ifReturnElseReturnTwo($x) {
        return !($x > 0);
    }
    public function ifReturnElseReturnThree($x) {
        if ($x > 0) { return true; }
        else { return true; }
    }
    public function ifReturnElseReturnFour($x) {
        if ($x === 0) { return true; }
        /* a comment here */
        return !($x > 0);
    }

    public function ifNotReturnElseReturn($x): bool {
        return !$this->ifNotReturnElseReturn();
        return $this->ifNotReturnElseReturn();

        return $this->ifNotReturnElseReturn();
        return !($this->ifNotReturnElseReturn());
    }

    function ifAssignElseAssignReturn($x) {
        return $result = $x === 0;
    }

    function assignIfAssignReturn($x) {
        return $result = $x === 0;
    }

    function assignIfReturnReturn($x) {
        return $x === 0;
    }
}
