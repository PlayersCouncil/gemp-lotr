package com.gempukku.lotro.cards.official.set13;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.CardType;
import com.gempukku.lotro.common.Phase;
import com.gempukku.lotro.common.Culture;
import com.gempukku.lotro.common.Side;
import com.gempukku.lotro.common.Timeword;
import com.gempukku.lotro.common.Zone;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_13_166_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("enemy", "13_166");
					put("enemy2", "13_166");
					put("tactician", "13_175");  // Uruk Tactician: [uruk-hai] Uruk-hai, strength 7; a skirmish ability used only on purpose
					put("tactician2", "13_175");
					put("saruman", "12_144");    // Saruman, Agent of the Dark Lord: [uruk-hai] minion who is no Uruk-hai (a Wizard), strength 8, lurker;
					                             // a skirmish ability used only on purpose, and a heal of himself when a companion is killed
					put("sentry", "10_84");      // Cirith Ungol Sentry: [sauron] Uruk-hai, strength 8, damage +1 only
					put("entranced", "13_163");  // Entranced Uruk: [uruk-hai] Uruk-hai, strength 9, lurker; a bar on discarding its possessions only

					// [uruk-hai] cards for the discard pile, of each kind, where none of them does anything there
					put("vigilant", "11_205");   // Vigilant Uruk: [uruk-hai] minion
					put("sword", "11_192");      // Isengard Sword: [uruk-hai] possession
					put("alliance", "12_141");   // Dark Alliance: [uruk-hai] condition
					put("furor", "11_187");      // Furor: [uruk-hai] event

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: resistance 6, strength 8; a maneuver ability only
					put("sam", "1_311");         // Sam, Son of Hamfast: resistance 5; a fellowship ability and a response when Frodo dies
					put("gimli", "11_8");        // Gimli, Lively Combatant: resistance 7, strength 6; may draw when he wins a skirmish
					put("elrond", "1_40");       // Elrond, Lord of Rivendell: ally (resistance 0), home site 3, strength 8
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NewEnemyStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 13
		 * Name: New Enemy
		 * Unique: False
		 * Side: Shadow
		 * Culture: Uruk-hai
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Skirmish
		 * Game Text: You may remove from the game 4 [uruk-hai] cards in your discard pile instead of paying the twilight cost for this card.<br>Make your [uruk-hai] minion skirmishing a companion with resistance 4 or less strength +2.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("enemy");

		assertEquals("New Enemy", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.URUK_HAI, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.SKIRMISH));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void MakesAnUrukHaiMinionSkirmishingACompanionOfResistance4StrengthPlus2UntilTheSkirmishIsOver() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveCompanionsToTable(aragorn, sam);

		var enemy = scn.GetShadowCard("enemy");
		var tactician = scn.GetShadowCard("tactician");
		var saruman = scn.GetShadowCard("saruman");
		var sentry = scn.GetShadowCard("sentry");
		var entranced = scn.GetShadowCard("entranced");
		scn.MoveCardsToHand(enemy);

		scn.StartGame();
		// Two burdens: Aragorn 6 - 2 = 4, Sam 5 - 2 = 3.
		scn.AddBurdens(2);
		assertEquals(4, scn.GetResistance(aragorn));
		assertEquals(3, scn.GetResistance(sam));
		scn.MoveMinionsToTable(tactician, saruman, sentry, entranced);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, tactician}, new PhysicalCardImpl[]{sam, entranced});
		scn.ShadowAssignToMinions(aragorn, saruman, sentry);
		// Both skirmishes have a lurker in them, so either may come first.
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(enemy));
		scn.ShadowPlayCard(enemy);

		// The [uruk-hai] Uruk-hai and the [uruk-hai] Wizard skirmishing Aragorn; not the [sauron] Uruk-hai beside them,
		// nor the [uruk-hai] Uruk-hai assigned to Sam, whose skirmish has not begun.
		assertTrue(scn.ShadowHasCardChoiceAvailable(tactician, saruman));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentry, entranced, aragorn));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(7, scn.GetStrength(tactician));
		scn.ShadowChooseCard(tactician);

		assertEquals(9, scn.GetStrength(tactician));
		assertEquals(8, scn.GetStrength(saruman));
		assertEquals(8, scn.GetStrength(sentry));
		assertInDiscard(enemy);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		// Aragorn's skirmish resolves; in Sam's, a phase of its own, the bonus is gone.
		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(sam);
		assertEquals(7, scn.GetStrength(tactician));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void AMinionSkirmishingACompanionOfResistance5OrAnAllyIsNotStrengthened() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(elrond);

		var enemy = scn.GetShadowCard("enemy");
		var enemy2 = scn.GetShadowCard("enemy2");
		var tactician = scn.GetShadowCard("tactician");
		var tactician2 = scn.GetShadowCard("tactician2");
		scn.MoveCardsToHand(enemy, enemy2);

		scn.StartGame();
		scn.SkipToSite(2);
		// Two burdens: Gimli 7 - 2 = 5.  Elrond is an ally, of resistance 0.
		scn.AddBurdens(2);
		assertEquals(5, scn.GetResistance(gimli));
		assertEquals(0, scn.GetResistance(elrond));
		scn.MoveMinionsToTable(tactician, tactician2);
		// At site 3, Elrond's home, the ally may be assigned.
		scn.SkipToAssignments();
		assertEquals(3, scn.GetCurrentSiteNumber());
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{elrond, tactician}, new PhysicalCardImpl[]{gimli, tactician2});
		scn.FreepsResolveSkirmish(elrond);
		scn.SetTwilight(4);
		scn.FreepsPass();

		// An ally skirmishing an [uruk-hai] Uruk-hai: the event is played, and nothing is strengthened.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(enemy));
		scn.ShadowPlayCard(enemy);
		assertInDiscard(enemy);
		assertEquals(7, scn.GetStrength(tactician));
		assertEquals(7, scn.GetStrength(tactician2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassCurrentPhaseActions();
		scn.FreepsResolveSkirmish(gimli);
		scn.FreepsPass();

		// A companion of resistance 5: the same.
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(enemy2));
		scn.ShadowPlayCard(enemy2);
		assertInDiscard(enemy2);
		assertEquals(7, scn.GetStrength(tactician2));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void NotPlayableInTheAssignmentPhaseAndPlayableOnceTheSkirmishIsOn() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var enemy = scn.GetShadowCard("enemy");
		var tactician = scn.GetShadowCard("tactician");
		scn.MoveCardsToHand(enemy);

		scn.StartGame();
		scn.AddBurdens(2);
		assertEquals(4, scn.GetResistance(aragorn));
		scn.MoveMinionsToTable(tactician);
		scn.SetTwilight(4);
		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.FreepsPass();

		// Not a skirmish phase: not playable, with the pool able to pay and a minion to strengthen.
		assertTrue(scn.AwaitingShadowAssignmentPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(enemy));

		scn.ShadowPass();
		scn.FreepsAssignToMinions(aragorn, tactician);
		scn.FreepsResolveSkirmish(aragorn);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(enemy));
	}

	@Test
	public void WithTheTwilightAndFourUrukHaiCardsBothWaysToPayAreAskedAndTheCardsAreRemovedFromTheGame() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var freepsFuror = scn.GetFreepsCard("furor");
		scn.MoveCompanionsToTable(aragorn);

		var enemy = scn.GetShadowCard("enemy");
		var tactician = scn.GetShadowCard("tactician");
		var vigilant = scn.GetShadowCard("vigilant");
		var sword = scn.GetShadowCard("sword");
		var alliance = scn.GetShadowCard("alliance");
		var saruman = scn.GetShadowCard("saruman");
		var sentry = scn.GetShadowCard("sentry");
		var entranced = scn.GetShadowCard("entranced");
		scn.MoveCardsToHand(enemy);
		// Five [uruk-hai] cards (two minions, a minion who is no Uruk-hai, a possession and a condition: one more than the
		// four to be removed), and a [sauron] Uruk-hai.
		scn.MoveCardsToDiscard(vigilant, sword, alliance, saruman, entranced, sentry);
		// The other player's [uruk-hai] card, in his own discard pile: a title the Shadow player's pile does not hold,
		// since a choice from a pile is read by blueprint.
		scn.MoveCardsToDiscard(freepsFuror);

		scn.StartGame();
		assertInDiscard(vigilant, sword, alliance, saruman, entranced, sentry, freepsFuror);
		scn.AddBurdens(2);
		assertEquals(4, scn.GetResistance(aragorn));
		scn.MoveMinionsToTable(tactician);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, tactician);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertEquals(2, scn.GetTwilight());
		assertEquals(7, scn.GetStrength(tactician));
		scn.ShadowPlayCard(enemy);
		assertTrue(scn.ShadowDecisionAvailable("remove cards from discard instead of paying twilight cost"));
		scn.ShadowChooseYes();

		// Exactly four of the five [uruk-hai] cards, of any type; not the [sauron] Uruk-hai, nor the other player's card.
		assertTrue(scn.ShadowHasCardChoiceAvailable(vigilant, sword, alliance, saruman, entranced));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentry, freepsFuror));
		assertEquals(4, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(vigilant, sword, alliance, saruman);

		assertInZone(Zone.REMOVED, vigilant, sword, alliance, saruman);
		assertInDiscard(entranced, sentry, freepsFuror);
		assertEquals(2, scn.GetTwilight());

		// The lone minion is chosen unasked.
		assertEquals(9, scn.GetStrength(tactician));
		assertInDiscard(enemy);
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void DecliningTheRemovalPaysTheTwilightCost() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var enemy = scn.GetShadowCard("enemy");
		var tactician = scn.GetShadowCard("tactician");
		var vigilant = scn.GetShadowCard("vigilant");
		var sword = scn.GetShadowCard("sword");
		var alliance = scn.GetShadowCard("alliance");
		var furor = scn.GetShadowCard("furor");
		scn.MoveCardsToHand(enemy);
		scn.MoveCardsToDiscard(vigilant, sword, alliance, furor);

		scn.StartGame();
		assertInDiscard(vigilant, sword, alliance, furor);
		scn.AddBurdens(2);
		assertEquals(4, scn.GetResistance(aragorn));
		scn.MoveMinionsToTable(tactician);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, tactician);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(enemy));
		assertEquals(7, scn.GetStrength(tactician));
		scn.ShadowPlayCard(enemy);
		assertTrue(scn.ShadowDecisionAvailable("remove cards from discard instead of paying twilight cost"));
		scn.ShadowChooseNo();

		// The twilight cost is paid in place of the removal; the lone minion is chosen unasked.
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(vigilant, sword, alliance, furor, enemy);
		assertEquals(9, scn.GetStrength(tactician));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithThreeUrukHaiCardsTheTwilightIsPaidUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var enemy = scn.GetShadowCard("enemy");
		var tactician = scn.GetShadowCard("tactician");
		var vigilant = scn.GetShadowCard("vigilant");
		var sword = scn.GetShadowCard("sword");
		var alliance = scn.GetShadowCard("alliance");
		var sentry = scn.GetShadowCard("sentry");
		scn.MoveCardsToHand(enemy);
		// Four cards, three of them [uruk-hai].
		scn.MoveCardsToDiscard(vigilant, sword, alliance, sentry);

		scn.StartGame();
		assertInDiscard(vigilant, sword, alliance, sentry);
		scn.AddBurdens(2);
		assertEquals(4, scn.GetResistance(aragorn));
		scn.MoveMinionsToTable(tactician);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, tactician);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(2);
		scn.FreepsPass();

		assertTrue(scn.ShadowPlayAvailable(enemy));
		scn.ShadowPlayCard(enemy);
		assertFalse(scn.ShadowDecisionAvailable("instead of paying twilight cost"));
		assertEquals(0, scn.GetTwilight());
		assertInDiscard(vigilant, sword, alliance, sentry, enemy);
		assertEquals(9, scn.GetStrength(tactician));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void WithAnEmptyPoolItIsPlayedOnlyWithFourUrukHaiCardsToRemoveAndTheyAreRemovedUnasked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var enemy = scn.GetShadowCard("enemy");
		var tactician = scn.GetShadowCard("tactician");
		var vigilant = scn.GetShadowCard("vigilant");
		var sword = scn.GetShadowCard("sword");
		var alliance = scn.GetShadowCard("alliance");
		var furor = scn.GetShadowCard("furor");
		var sentry = scn.GetShadowCard("sentry");
		scn.MoveCardsToHand(enemy);
		// Four cards, three of them [uruk-hai].
		scn.MoveCardsToDiscard(vigilant, sword, alliance, sentry);

		scn.StartGame();
		assertInDiscard(vigilant, sword, alliance, sentry);
		scn.AddBurdens(2);
		assertEquals(4, scn.GetResistance(aragorn));
		scn.MoveMinionsToTable(tactician);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, tactician);
		scn.FreepsResolveSkirmish(aragorn);
		scn.SetTwilight(0);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(enemy));

		// A fourth [uruk-hai] card: the removal is the only way to pay, and it is taken with no question.
		scn.MoveCardsToDiscard(furor);
		assertInDiscard(furor);
		scn.ShadowRefreshDecision();
		assertTrue(scn.ShadowPlayAvailable(enemy));
		assertEquals(7, scn.GetStrength(tactician));
		scn.ShadowPlayCard(enemy);

		assertFalse(scn.ShadowDecisionAvailable("instead of paying twilight cost"));
		assertTrue(scn.ShadowDecisionAvailable("Choose cards to remove"));
		assertTrue(scn.ShadowHasCardChoiceAvailable(vigilant, sword, alliance, furor));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sentry));
		scn.ShadowChooseCards(vigilant, sword, alliance, furor);
		assertInZone(Zone.REMOVED, vigilant, sword, alliance, furor);
		assertInDiscard(sentry, enemy);
		assertEquals(0, scn.GetTwilight());
		assertEquals(9, scn.GetStrength(tactician));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
