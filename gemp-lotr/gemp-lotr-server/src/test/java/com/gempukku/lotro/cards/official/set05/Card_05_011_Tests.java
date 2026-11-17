package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.assertInDiscard;
import static com.gempukku.lotro.framework.Assertions.assertInHand;
import static org.junit.Assert.*;

public class Card_05_011_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("charge", "5_11");
					// Elf companions who are no archers, so that the fellowship archery total is 0 without the event
					put("legolas", "11_21");    // Legolas, Companion of the Ring: strength 6, vitality 3; +2 only at a battleground or forest
					put("arwen", "1_30");       // Arwen, Daughter of Elrond: vitality 3; Ranger, +3 only against a Nazgul
					put("haldir", "1_48");      // Haldir, Elf of the Golden Wood: vitality 3; +2 only at sites 6 to 8
					put("lorien", "1_53");      // Lorien Elf: vitality 2, no text but its play requirement
					put("lorien2", "1_53");
					put("celeborn", "1_34");    // Celeborn, Lord of Lorien: an Elf ally, vitality 3; a fellowship ability only
					put("gimli", "1_13");       // Gimli, Son of Gloin: a companion who is no Elf; a skirmish ability only

					put("party", "1_158");      // Uruk-hai Raiding Party: strength 9, vitality 3, damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BreaktheChargeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Break the Charge
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Elven
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Archery
		 * Game Text: <b>Archery:</b> Exert 3 Elf companions to make the fellowship archery total +2.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("charge");

		assertEquals("Break the Charge", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.ELVEN, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.ARCHERY));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertingThreeElfCompanionsMakesTheFellowshipArcheryTotalTwoForTheArcheryPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var charge = scn.GetFreepsCard("charge");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var lorien = scn.GetFreepsCard("lorien");
		var lorien2 = scn.GetFreepsCard("lorien2");
		var celeborn = scn.GetFreepsCard("celeborn");
		var gimli = scn.GetFreepsCard("gimli");
		var frodo = scn.GetRingBearer();
		scn.MoveCardsToHand(charge);
		scn.MoveCompanionsToTable(legolas, arwen, haldir, lorien, lorien2, gimli);
		scn.MoveAlliesToTable(celeborn);
		// Vitality 2 with 1 wound: this Lorien Elf is exhausted.
		scn.AddWoundsToChar(lorien2, 1);

		var party = scn.GetShadowCard("party");

		scn.StartGame();
		assertEquals(1, scn.GetWoundsOn(lorien2));
		scn.MoveMinionsToTable(party);
		scn.SkipToPhase(Phase.ARCHERY);

		assertEquals(0, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());
		assertTrue(scn.FreepsPlayAvailable(charge));
		scn.FreepsPlayCard(charge);

		// Elf companions who can exert; not the exhausted one, an Elf ally, a companion who is no Elf, or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(legolas, arwen, haldir, lorien));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(lorien2, celeborn, gimli, frodo));
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(legolas, arwen, haldir);

		assertEquals(1, scn.GetWoundsOn(legolas));
		assertEquals(1, scn.GetWoundsOn(arwen));
		assertEquals(1, scn.GetWoundsOn(haldir));
		assertEquals(0, scn.GetWoundsOn(lorien));
		assertEquals(0, scn.GetWoundsOn(celeborn));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertInDiscard(charge);

		// Still the archery phase: the fellowship's total is 2, the minions' is untouched.
		assertTrue(scn.AwaitingShadowArcheryPhaseActions());
		assertEquals(2, scn.GetFreepsArcheryTotal());
		assertEquals(0, scn.GetShadowArcheryTotal());

		// Both pass: the two arrows land on the one minion, unasked.
		assertEquals(0, scn.GetWoundsOn(party));
		scn.ShadowPass();
		scn.FreepsPass();
		assertEquals(2, scn.GetWoundsOn(party));

		// The archery phase is over, and the bonus with it.
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		assertEquals(0, scn.GetFreepsArcheryTotal());
	}

	@Test
	public void CannotBePlayedWithOnlyTwoElfCompanionsAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var charge = scn.GetFreepsCard("charge");
		var legolas = scn.GetFreepsCard("legolas");
		var arwen = scn.GetFreepsCard("arwen");
		var haldir = scn.GetFreepsCard("haldir");
		var celeborn = scn.GetFreepsCard("celeborn");
		var gimli = scn.GetFreepsCard("gimli");
		scn.MoveCardsToHand(charge);
		scn.MoveCompanionsToTable(legolas, arwen, haldir, gimli);
		scn.MoveAlliesToTable(celeborn);
		// Vitality 3 with 2 wounds: Haldir is exhausted.  An Elf ally and a Dwarf could exert, and do not count.
		scn.AddWoundsToChar(haldir, 2);

		var party = scn.GetShadowCard("party");

		scn.StartGame();
		assertEquals(2, scn.GetWoundsOn(haldir));
		scn.MoveMinionsToTable(party);
		scn.SkipToPhase(Phase.ARCHERY);

		assertTrue(scn.AwaitingFreepsArcheryPhaseActions());
		assertInHand(charge);
		assertFalse(scn.FreepsPlayAvailable(charge));
		assertEquals(0, scn.GetWoundsOn(legolas));
		assertEquals(0, scn.GetWoundsOn(arwen));
	}
}
