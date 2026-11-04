package com.gempukku.lotro.cards.official.set18;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_18_042_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("denethor", "18_42");

					put("knight", "5_35");      // Gondorian Knight: [gondor] Man companion, strength 6, vitality 3; Knight is his whole text
					put("merchant", "7_97");    // Gondorian Merchant: the [gondor] Man ally, vitality 1; a maneuver ability used only on purpose
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: vitality +1, so that the Merchant can exert; its response answers wounds only
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] companion who is no Man (a Hobbit); a skirmish ability used only on purpose
					put("eomer", "4_267");      // Eomer, Third Marshal of Riddermark: a [rohan] Man, vitality 3; his strength text reads a skirmish
					put("sword", "18_43");      // Denethor's Sword: "While bearer is Denethor, his game text does not apply."

					put("runner1", "1_178");    // Goblin Runner: a minion with no text once in play, strength 5
					put("runner2", "1_178");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DenethorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 18
		 * Name: Denethor, On the Edge of Madness
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 8
		 * Vitality: 3
		 * Resistance: 5
		 * Game Text: Each time the fellowship moves from a site, add a burden.<br><b>Skirmish:</b> Exert another [gondor] Man to make him or her strength +1 and <b>damage +1</b>.
		*/

		var scn = GetScenario();

		var denethor = scn.GetFreepsCard("denethor");

		assertEquals("Denethor", denethor.getBlueprint().getTitle());
		assertEquals("On the Edge of Madness", denethor.getBlueprint().getSubtitle());
		assertTrue(denethor.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, denethor.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, denethor.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, denethor.getBlueprint().getCardType());
		assertEquals(Race.MAN, denethor.getBlueprint().getRace());
		assertEquals(2, denethor.getBlueprint().getTwilightCost());
		assertEquals(8, denethor.getBlueprint().getStrength());
		assertEquals(3, denethor.getBlueprint().getVitality());
		assertEquals(5, denethor.getBlueprint().getResistance());
	}

	@Test
	public void EachMoveFromASiteAddsABurdenTheRegroupMoveIncluded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denethor = scn.GetFreepsCard("denethor");
		scn.MoveCompanionsToTable(denethor);

		scn.StartGame();
		assertEquals(0, scn.GetBurdens());
		assertEquals(1, scn.GetCurrentSiteNumber());

		// The fellowship phase's move, from site 1.
		scn.FreepsPass();
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(1, scn.GetBurdens());

		scn.SkipToMovementDecision();
		assertEquals(1, scn.GetBurdens());
		// The regroup move, from site 2.
		scn.FreepsChooseToMove();
		assertEquals(3, scn.GetCurrentSiteNumber());
		assertEquals(2, scn.GetBurdens());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void SkirmishAbilityExertsAnotherGondorManToMakeHimStrengthPlus1AndDamagePlus1ForThatSkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var denethor = scn.GetFreepsCard("denethor");
		var knight = scn.GetFreepsCard("knight");
		var merchant = scn.GetFreepsCard("merchant");
		var narsil = scn.GetFreepsCard("narsil");
		var pippin = scn.GetFreepsCard("pippin");
		var eomer = scn.GetFreepsCard("eomer");
		scn.MoveCompanionsToTable(denethor, knight, pippin, eomer);
		scn.MoveAlliesToTable(merchant);
		scn.AttachCardsTo(merchant, narsil);
		assertAttachedTo(narsil, merchant);

		var runner1 = scn.GetShadowCard("runner1");
		var runner2 = scn.GetShadowCard("runner2");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1, runner2);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(
				new PhysicalCardImpl[]{knight, runner1},
				new PhysicalCardImpl[]{eomer, runner2});
		scn.FreepsResolveSkirmish(knight);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(denethor));
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(0, scn.GetKeywordCount(knight, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(denethor));
		assertEquals(0, scn.GetKeywordCount(denethor, Keyword.DAMAGE));
		// Denethor is in no skirmish: the ability is his all the same.
		assertFalse(scn.IsCharAssigned(denethor));
		assertTrue(scn.FreepsActionAvailable(denethor));

		scn.FreepsUseCardAction(denethor);
		// Another [gondor] Man, companion or ally; not Denethor himself, the [gondor] Hobbit, the [rohan] Man or Frodo.
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, merchant));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(denethor, pippin, eomer, frodo));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(knight);

		// The Man who exerted gains the bonus; Denethor and the Merchant do not.
		assertEquals(1, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(denethor));
		assertEquals(7, scn.GetStrength(knight));
		assertEquals(1, scn.GetKeywordCount(knight, Keyword.DAMAGE));
		assertEquals(8, scn.GetStrength(denethor));
		assertEquals(0, scn.GetKeywordCount(denethor, Keyword.DAMAGE));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(eomer);
		// In the next skirmish, a phase of its own, the bonus is gone.
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertEquals(6, scn.GetStrength(knight));
		assertEquals(0, scn.GetKeywordCount(knight, Keyword.DAMAGE));
	}

	@Test
	public void SkirmishAbilityIsNotOfferedWithTheOnlyOtherGondorManExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denethor = scn.GetFreepsCard("denethor");
		var knight = scn.GetFreepsCard("knight");
		var eomer = scn.GetFreepsCard("eomer");
		scn.MoveCompanionsToTable(denethor, knight, eomer);
		// Vitality 3 with 2 wounds: exhausted.  Denethor himself and Eomer could still exert.
		scn.AddWoundsToChar(knight, 2);
		assertEquals(2, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(denethor));
		assertEquals(0, scn.GetWoundsOn(eomer));

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(eomer, runner1);
		scn.FreepsResolveSkirmish(eomer);

		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(denethor));
	}

	@Test
	public void BearingDenethorsSwordHisGameTextDoesNotApply() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var denethor = scn.GetFreepsCard("denethor");
		var knight = scn.GetFreepsCard("knight");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(denethor, knight);
		scn.AttachCardsTo(denethor, sword);
		assertAttachedTo(sword, denethor);

		var runner1 = scn.GetShadowCard("runner1");

		scn.StartGame();
		scn.MoveMinionsToTable(runner1);
		assertEquals(0, scn.GetBurdens());

		scn.FreepsPass();
		// The move from site 1 adds no burden.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(0, scn.GetBurdens());

		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(knight, runner1);
		scn.FreepsResolveSkirmish(knight);

		// The Knight could exert, and the Skirmish ability is not offered.
		assertEquals(0, scn.GetWoundsOn(knight));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertFalse(scn.FreepsActionAvailable(denethor));
	}
}
