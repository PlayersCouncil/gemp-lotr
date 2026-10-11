package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_040_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("demands", "2_40");
					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, cost 2, site 5; damage +1 and nothing else
					put("goblinman", "2_42");   // Goblin Man: [isengard] Orc; discarded underground; a Shadow ability used only on purpose
					put("runner", "1_178");     // Goblin Runner: a [moria] minion, strength 5, vitality 1; cheated in, so its text does nothing
					put("armory", "1_157");     // Uruk-hai Armory: an [isengard] condition in the support area; its text is about archery

					put("rosie", "1_309");      // Rosie Cotton: [shire] Hobbit ally, vitality 2. "Fellowship: Exert Rosie Cotton to heal Sam."
					put("gaffer", "1_291");     // The Gaffer: [shire] Hobbit ally, vitality 2. "Fellowship: Exert The Gaffer to heal Frodo or Sam."
					put("tom", "9_52");         // Tom Bombadil: a [shire] ally who is no Hobbit, vitality 9. "Skirmish: Spot 2 [shire] companions and exert Tom Bombadil X times, where X is the fellowship's site number, to cancel a skirmish involving a [shire] companion."
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an [elven] ally. "Fellowship: Exert Elrond to draw a card."
					put("sam", "1_311");        // Sam, Son of Hamfast: a [shire] Hobbit who is a companion. "Fellowship: Exert Sam to remove a burden."
					put("assassin", "2_93");    // Tower Assassin: [sauron] Orc, vitality 3. "Maneuver: Exert Tower Assassin to wound an ally."
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DemandsofthebrSackvilleBagginsesStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Demands of the Sackville-Bagginses
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 1
		 * Type: Condition
		 * Subtype: Support Area
		 * Game Text: To play, spot an [isengard] minion. Plays to your support area.
		 * 		Each time a [shire] ally exerts, add (1).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("demands");

		assertEquals("Demands of the Sackville-Bagginses", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void DemandsoftheSackvilleBagginsesRequiresIsengardMinionToPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var demands = scn.GetShadowCard("demands");
		var savage = scn.GetShadowCard("savage");
		var runner = scn.GetShadowCard("runner");
		var armory = scn.GetShadowCard("armory");
		scn.MoveCardsToHand(demands, savage);

		scn.StartGame();
		// A [moria] minion and an [isengard] condition: neither is an [isengard] minion.
		scn.MoveMinionsToTable(runner);
		scn.MoveCardsToSupportArea(armory);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertInZone(Zone.HAND, demands);
		assertFalse(scn.ShadowPlayAvailable(demands));

		scn.ShadowPlayCard(savage);
		assertTrue(scn.ShadowPlayAvailable(demands));

		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(demands);
		assertEquals(twilight - 1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, demands);
		assertNotAttachedTo(demands, savage);
		assertNotAttachedTo(demands, scn.GetRingBearer());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AnIsengardOrcMayBeSpottedToPlayIt() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var demands = scn.GetShadowCard("demands");
		var goblinman = scn.GetShadowCard("goblinman");
		scn.MoveCardsToHand(demands);

		scn.StartGame();
		scn.MoveMinionsToTable(goblinman);
		scn.SetTwilight(10);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(demands));

		int twilight = scn.GetTwilight();
		scn.ShadowPlayCard(demands);
		assertEquals(twilight - 1, scn.GetTwilight());
		assertInZone(Zone.SUPPORT, demands);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DemandsoftheSackvilleBagginsesAdds1WhenShireAllyExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var rosie = scn.GetFreepsCard("rosie");
		var gaffer = scn.GetFreepsCard("gaffer");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveAlliesToTable(rosie, gaffer);
		scn.MoveCompanionsToTable(sam);

		var demands = scn.GetShadowCard("demands");
		scn.MoveCardsToSupportArea(demands);

		scn.StartGame();
		scn.AddWoundsToChar(sam, 2);

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(rosie));

		scn.FreepsUseCardAction(rosie);
		assertEquals(1, scn.GetWoundsOn(rosie));
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetTwilight());

		// And again for the next [shire] ally to exert. Frodo is unwounded, so The Gaffer's heal is Sam's.
		scn.FreepsUseCardAction(gaffer);
		assertEquals(1, scn.GetWoundsOn(gaffer));
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(2, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AShireAllyWhoExertsTwiceForOneCostAdds2() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var tom = scn.GetFreepsCard("tom");
		var sam = scn.GetFreepsCard("sam");
		scn.MoveAlliesToTable(tom);
		scn.MoveCompanionsToTable(sam);

		var demands = scn.GetShadowCard("demands");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToSupportArea(demands);

		scn.StartGame();
		scn.MoveMinionsToTable(runner);

		scn.SkipToPhase(Phase.ASSIGNMENT);
		scn.PassCurrentPhaseActions();
		scn.FreepsAssignToMinions(sam, runner);
		scn.FreepsResolveSkirmish(sam);

		// The fellowship is at site 2, so Tom Bombadil, a [shire] ally who is no Hobbit, exerts twice.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
		assertTrue(scn.IsCharSkirmishingAgainst(sam, runner));
		assertEquals(0, scn.GetWoundsOn(tom));
		int twilight = scn.GetTwilight();

		scn.FreepsUseCardAction(tom);
		assertEquals(2, scn.GetWoundsOn(tom));
		// (1) for each of the two exertions
		assertEquals(twilight + 2, scn.GetTwilight());
		// The skirmish is cancelled, and with no other the regroup phase follows.
		assertFalse(scn.IsCharSkirmishing(sam));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void DemandsoftheSackvilleBagginsesAddsNothingWhenNonShireAllyExerts() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveAlliesToTable(elrond);

		var demands = scn.GetShadowCard("demands");
		scn.MoveCardsToSupportArea(demands);

		scn.StartGame();

		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(elrond));
		assertEquals(0, scn.GetFreepsHandCount());

		scn.FreepsUseCardAction(elrond);
		assertEquals(1, scn.GetWoundsOn(elrond));
		assertEquals(1, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetTwilight());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AShireCompanionExertingOrAShireAllyBeingWoundedAddsNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var sam = scn.GetFreepsCard("sam");
		var rosie = scn.GetFreepsCard("rosie");
		var elrond = scn.GetFreepsCard("elrond");
		scn.MoveCompanionsToTable(sam);
		scn.MoveAlliesToTable(rosie, elrond);

		var demands = scn.GetShadowCard("demands");
		var assassin = scn.GetShadowCard("assassin");
		scn.MoveCardsToSupportArea(demands);

		scn.StartGame();
		scn.AddBurdens(1);

		// Sam is a [shire] Hobbit, but a companion.
		assertEquals(0, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(sam));
		assertEquals(1, scn.GetBurdens());

		scn.FreepsUseCardAction(sam);
		assertEquals(1, scn.GetWoundsOn(sam));
		assertEquals(0, scn.GetBurdens());
		assertEquals(0, scn.GetTwilight());

		scn.MoveMinionsToTable(assassin);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// A [shire] ally takes a wound that is no exertion (and the minion that exerts for it is no ally).
		int twilight = scn.GetTwilight();
		scn.ShadowUseCardAction(assassin);
		assertTrue(scn.ShadowHasCardChoiceAvailable(rosie, elrond));
		scn.ShadowChooseCard(rosie);
		assertEquals(1, scn.GetWoundsOn(assassin));
		assertEquals(1, scn.GetWoundsOn(rosie));
		assertEquals(twilight, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
