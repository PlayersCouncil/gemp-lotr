package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_127_Tests
{

	// Towers sites, as for Haldir 4_71: their texts do not reach these tests.  Sites are put in either player's
	// hands by the rig once every marker has passed them.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("vorondil", "7_127");
					// Aragorn, Ranger of the North: [gondor] Man companion, no knight, strength 8, vitality 4; a maneuver
					// ability used only on purpose
					put("aragorn", "1_89");
					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion and a knight, vitality 3; Knight is its whole text
					// Knight of Gondor: [gondor] Man companion and a knight, vitality 3; a fellowship ability used only on purpose
					put("kog", "5_36");
					// Pippin, Trollslayer: a [gondor] companion who is no Man (a Hobbit), and a knight, vitality 4; a
					// skirmish ability used only on purpose
					put("pippin", "103_31");
					// Gondorian Merchant: the [gondor] Man ally, vitality 1; his maneuver ability is used only on purpose
					put("merchant", "7_97");
					// Narsil, Blade of the Faithful: vitality +1 on a [gondor] Man, so that the Merchant can exert; its
					// response answers a wound to its bearer, who takes none here
					put("narsil", "9_34");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, no knight, vitality 3; Valiant and nothing else
				}},
				VirtualTableScenario.TowersSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void VorondilStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Vorondil
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 5
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: While no opponent controls a site, Vorondil is strength +2.<br><b>Regroup:</b> Exert 3 [gondor] Men or 2 knights to liberate a site.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("vorondil");

		assertEquals("Vorondil", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(5, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
	}

	@Test
	public void StrengthPlusTwoWhileNoOpponentControlsASite() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vorondil = scn.GetFreepsCard("vorondil");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(vorondil, aragorn);

		scn.StartGame();
		scn.SkipToSite(3);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);

		// 5 + 2; Aragorn, another [gondor] Man companion, is his printed 8 throughout.
		assertFalse(scn.IsSiteControlled(site1));
		assertFalse(scn.IsSiteControlled(site2));
		assertEquals(7, scn.GetStrength(vorondil));
		assertEquals(8, scn.GetStrength(aragorn));

		// A site his own player controls is not one an opponent controls.
		scn.FreepsTakeControlOfSite(site1);
		assertTrue(scn.FreepsControls(site1));
		assertEquals(7, scn.GetStrength(vorondil));
		assertEquals(8, scn.GetStrength(aragorn));

		scn.ShadowTakeControlOfSite(site2);
		assertTrue(scn.ShadowControls(site2));
		assertEquals(5, scn.GetStrength(vorondil));
		assertEquals(8, scn.GetStrength(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WithBothPricesOpenThreeGondorMenExertToLiberateTheHighestSiteAnOpponentControls() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var vorondil = scn.GetFreepsCard("vorondil");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var guard = scn.GetFreepsCard("guard");
		// Two knights: the Gondorian Knight and Pippin.
		scn.MoveCompanionsToTable(vorondil, aragorn, knight, pippin, guard);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		var site3 = scn.GetSite(3);
		scn.ShadowTakeControlOfSite(site1);
		scn.FreepsTakeControlOfSite(site2);
		scn.ShadowTakeControlOfSite(site3);
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(vorondil));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(2, scn.GetShadowControlledSiteCount());
		assertEquals(5, scn.GetStrength(vorondil));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertTrue(scn.FreepsActionAvailable(vorondil));

		scn.FreepsUseCardAction(vorondil);

		// Either price can be paid, so the player is asked which.
		assertEquals(2, scn.FreepsGetMultipleChoiceCount());
		assertTrue(scn.FreepsChoiceAvailable("Exert 3"));
		assertTrue(scn.FreepsChoiceAvailable("knights"));
		scn.FreepsChooseOption("Exert 3");

		// [gondor] Men, Vorondil himself among them, companions and the ally; not a [gondor] Hobbit, a [rohan] Man
		// or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(vorondil, aragorn, knight, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(pippin, guard, frodo));
		assertEquals(3, scn.FreepsGetChoiceMin());
		assertEquals(3, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(vorondil, aragorn, merchant);
		assertEquals(1, scn.GetWoundsOn(vorondil));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(guard));

		// Site 3 is the highest-numbered site an opponent controls: back on the adventure path.
		assertInZone(Zone.ADVENTURE_PATH, site3);
		assertFalse(scn.IsSiteControlled(site3));
		assertTrue(scn.ShadowControls(site1));
		assertTrue(scn.FreepsControls(site2));
		assertEquals(5, scn.GetStrength(vorondil));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void ExertingTwoKnightsLiberatesTheLastSiteAnOpponentControlsAndHeIsStrengthPlusTwoAgain() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var vorondil = scn.GetFreepsCard("vorondil");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var kog = scn.GetFreepsCard("kog");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(vorondil, aragorn, knight, kog, pippin, guard);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		var site2 = scn.GetSite(2);
		scn.ShadowTakeControlOfSite(site1);
		scn.FreepsTakeControlOfSite(site2);
		// Vorondil (vitality 3) and Aragorn (vitality 4) exhausted: only the two Knights are [gondor] Men able to
		// exert, so the knights are the one price open.
		scn.AddWoundsToChar(vorondil, 2);
		scn.AddWoundsToChar(aragorn, 3);
		assertEquals(2, scn.GetWoundsOn(vorondil));
		assertEquals(3, scn.GetWoundsOn(aragorn));
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(kog));
		assertEquals(0, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertEquals(5, scn.GetStrength(vorondil));
		assertTrue(scn.FreepsActionAvailable(vorondil));
		scn.FreepsUseCardAction(vorondil);

		// Knights, two Men and a Hobbit; not the Household Guard, a Man who could exert and is no knight, or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, kog, pippin));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(guard, frodo, vorondil, aragorn));
		assertEquals(2, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(knight, pippin);
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(1, scn.GetWoundsOn(pippin));
		assertEquals(0, scn.GetWoundsOn(kog));
		assertEquals(0, scn.GetWoundsOn(guard));

		// Site 2 is his own player's, so site 1 is liberated, and no opponent controls a site: 5 + 2.
		assertInZone(Zone.ADVENTURE_PATH, site1);
		assertFalse(scn.IsSiteControlled(site1));
		assertTrue(scn.FreepsControls(site2));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(7, scn.GetStrength(vorondil));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoSiteAnOpponentControlsItIsOfferedAndLiberatesNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vorondil = scn.GetFreepsCard("vorondil");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		// Three [gondor] Men and one knight: the exertion of all three is the one price open.
		scn.MoveCompanionsToTable(vorondil, aragorn, knight);

		scn.StartGame();
		scn.SkipToSite(4);
		var site1 = scn.GetSite(1);
		scn.FreepsTakeControlOfSite(site1);
		scn.SkipToPhase(Phase.REGROUP);

		assertTrue(scn.FreepsControls(site1));
		assertEquals(0, scn.GetShadowControlledSiteCount());
		assertEquals(0, scn.GetWoundsOn(vorondil));
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(7, scn.GetStrength(vorondil));
		assertTrue(scn.FreepsActionAvailable(vorondil));
		scn.FreepsUseCardAction(vorondil);

		// The cost is paid, and nothing is liberated: his own site is no opponent's.
		assertEquals(1, scn.GetWoundsOn(vorondil));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(knight));
		assertTrue(scn.FreepsControls(site1));
		assertEquals(7, scn.GetStrength(vorondil));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void NotOfferedWithTwoGondorMenAndOneKnightAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var vorondil = scn.GetFreepsCard("vorondil");
		var aragorn = scn.GetFreepsCard("aragorn");
		var knight = scn.GetFreepsCard("knight");
		var pippin = scn.GetFreepsCard("pippin");
		var guard = scn.GetFreepsCard("guard");
		scn.MoveCompanionsToTable(vorondil, aragorn, knight, pippin, guard);

		scn.StartGame();
		scn.SkipToSite(4);
		scn.ShadowTakeControlOfSite(scn.GetSite(1));
		// Aragorn (vitality 4), a third [gondor] Man, and Pippin (vitality 4), a second knight, exhausted.  The
		// Household Guard could exert, but is neither a [gondor] Man nor a knight.
		scn.AddWoundsToChar(aragorn, 3);
		scn.AddWoundsToChar(pippin, 3);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertEquals(3, scn.GetWoundsOn(pippin));
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(1, scn.GetShadowControlledSiteCount());
		assertEquals(0, scn.GetWoundsOn(vorondil));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(guard));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(vorondil));
	}
}
