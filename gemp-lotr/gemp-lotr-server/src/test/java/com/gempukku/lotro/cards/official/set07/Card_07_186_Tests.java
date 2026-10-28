package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_186_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("axe", "7_186");
					// Morgul Hound: a [wraith] Orc, strength 5, vitality 2; strength +4 and fierce at 2 wounds on the
					// Ring-bearer, no damage bonus of its own
					put("hound", "7_192");
					// Morgul Mongrel: a [wraith] Orc of another title, strength 7; fierce and damage +1 only at 3 wounds on
					// the Ring-bearer
					put("mongrel", "7_194");
					// Ulaire Lemenya, Lieutenant of Morgul: a [wraith] minion who is no Orc (a Nazgul); her text reaches only
					// a companion or ally bearing a [wraith] condition
					put("lemenya", "1_232");
					// Goblin Runner: an Orc of another culture ([moria]); its text answers only its own play
					put("runner", "1_178");
					// Morgul Brute: played with a Nazgul to spot, it may add a burden, which the Free Peoples player may
					// prevent by wounding the Ring-bearer (the legal wound on him)
					put("brute", "7_188");
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound, not the Ring-bearer; vitality 4
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void MorgulAxeStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Morgul Axe
		 * Unique: False
		 * Side: Shadow
		 * Culture: Wraith
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand weapon
		 * Strength: 2
		 * Game Text: Bearer must be a [wraith] Orc.
		 * While you can spot 2 wounds on the Ring-bearer, bearer is <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var axe = scn.GetFreepsCard("axe");

		assertEquals("Morgul Axe", axe.getBlueprint().getTitle());
		assertNull(axe.getBlueprint().getSubtitle());
		assertFalse(axe.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, axe.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, axe.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, axe.getBlueprint().getCardType());
		assertTrue(axe.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, axe.getBlueprint().getTwilightCost());
		assertEquals(2, axe.getBlueprint().getStrength());
	}

	@Test
	public void IsPlayedOnlyOnAWraithOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var axe = scn.GetShadowCard("axe");
		var hound = scn.GetShadowCard("hound");
		var mongrel = scn.GetShadowCard("mongrel");
		var lemenya = scn.GetShadowCard("lemenya");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(axe);
		assertInHand(axe);

		scn.StartGame();
		scn.MoveMinionsToTable(hound, mongrel, lemenya, runner);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(axe));
		scn.ShadowPlayCard(axe);

		// Two [wraith] Orcs of different titles; not a [wraith] Nazgul, nor a [moria] Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(hound, mongrel));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(lemenya, runner));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(hound);
		assertAttachedTo(axe, hound);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void BearerIsDamagePlus1OnceTheRingBearerHas2Wounds() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(sam);
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(sam, 2);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(sam));

		var axe = scn.GetShadowCard("axe");
		var hound = scn.GetShadowCard("hound");
		var mongrel = scn.GetShadowCard("mongrel");
		var lemenya = scn.GetShadowCard("lemenya");
		var brute = scn.GetShadowCard("brute");
		scn.MoveCardsToHand(brute);
		assertInHand(brute);

		scn.StartGame();
		scn.MoveMinionsToTable(hound, mongrel, lemenya);
		scn.AttachCardsTo(hound, axe);
		assertAttachedTo(axe, hound);
		scn.SetTwilight(10);
		scn.FreepsPass();

		// One wound on the Ring-bearer, and two on Sam, who is Ring-bound but not the Ring-bearer.
		assertEquals(0, scn.GetKeywordCount(hound, Keyword.DAMAGE));
		assertEquals(0, scn.GetKeywordCount(mongrel, Keyword.DAMAGE));

		// The Brute's burden, prevented by a wound on the Ring-bearer.
		scn.ShadowPlayCard(brute);
		scn.ShadowAcceptOptionalTrigger();
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();
		assertEquals(2, scn.GetWoundsOn(frodo));

		assertEquals(1, scn.GetKeywordCount(hound, Keyword.DAMAGE));
		// Another [wraith] Orc, not the bearer.
		assertEquals(0, scn.GetKeywordCount(mongrel, Keyword.DAMAGE));

		// Two wounds can still be spotted with three on him.
		scn.AddWoundsToChar(frodo, 1);
		assertEquals(3, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetKeywordCount(hound, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
