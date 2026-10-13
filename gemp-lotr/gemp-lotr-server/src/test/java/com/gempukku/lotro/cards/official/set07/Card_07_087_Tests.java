package com.gempukku.lotro.cards.official.set07;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_07_087_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("derufin", "7_87");
					put("armor", "1_92");       // Armor: [gondor] possession, bearer must be a Man; caps the wounds of a skirmish
					put("narsil", "9_34");      // Narsil, Blade of the Faithful: [gondor] artifact, bearer must be a [gondor] Man; a response to a wound

					// Great Siege-towers: a [sauron] condition that is an engine; Shadow and skirmish abilities never used.
					put("towers", "7_281");
					// Battering Ram: an [isengard] condition that is a machine; Shadow and skirmish abilities never used.
					put("ram", "5_44");
					// Seeking Its Master: a [sauron] condition that is no engine; a Shadow ability never used.
					put("seeking", "1_276");
					// Uruk-hai Armory: an [isengard] condition that is no machine; it reads the archery total only.
					put("armory", "1_157");
					// Orc Soldier: a [sauron] Orc; a skirmish ability never used.
					put("soldier", "1_271");
					// Banner of the Eye: a [sauron] possession, a hand weapon borne by a [sauron] Orc; it answers a won skirmish.
					put("banner", "6_98");

					// Cards that only fill a hand: Gondorian Knight, a [gondor] companion whose whole text is Knight.
					put("card1", "5_35");
					put("card2", "5_35");
					put("card3", "5_35");
					put("card4", "5_35");
					put("card5", "5_35");
					put("card6", "5_35");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void DerufinStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 7
		 * Name: Derufin
		 * Unique: True
		 * Side: Free Peoples
		 * Culture: Gondor
		 * Twilight Cost: 2
		 * Type: Companion
		 * Subtype: Man
		 * Strength: 6
		 * Vitality: 3
		 * Resistance: 6
		 * Game Text: <b>Regroup:</b> While you have exactly 5 cards in hand, you may exert Derufin to discard an
		 * 	engine, machine, or possession.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("derufin");

		assertEquals("Derufin", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GONDOR, card.getBlueprint().getCulture());
		assertEquals(CardType.COMPANION, card.getBlueprint().getCardType());
		assertEquals(Race.MAN, card.getBlueprint().getRace());
		assertEquals(2, card.getBlueprint().getTwilightCost());
		assertEquals(6, card.getBlueprint().getStrength());
		assertEquals(3, card.getBlueprint().getVitality());
		assertEquals(6, card.getBlueprint().getResistance());
		assertNull(card.getBlueprint().getSignet());
	}

	@Test
	public void RegroupAbilityWithExactlyFiveCardsInHandExertsDerufinToDiscardAnEngineMachineOrPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var derufin = scn.GetFreepsCard("derufin");
		var armor = scn.GetFreepsCard("armor");
		var narsil = scn.GetFreepsCard("narsil");
		scn.MoveCompanionsToTable(derufin);
		scn.AttachCardsTo(derufin, armor, narsil);
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"), scn.GetFreepsCard("card4"), scn.GetFreepsCard("card5"));

		var towers = scn.GetShadowCard("towers");
		var ram = scn.GetShadowCard("ram");
		var seeking = scn.GetShadowCard("seeking");
		var armory = scn.GetShadowCard("armory");
		var soldier = scn.GetShadowCard("soldier");
		var banner = scn.GetShadowCard("banner");
		scn.MoveCardsToSupportArea(towers, ram, seeking, armory);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);
		// A minion that lived through the skirmishes, bearing a Shadow possession.
		scn.MoveMinionsToTable(soldier);
		scn.AttachCardsTo(soldier, banner);
		scn.FreepsRefreshDecision();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(derufin));
		assertTrue(scn.FreepsActionAvailable(derufin));

		scn.FreepsUseCardAction(derufin);
		assertEquals(1, scn.GetWoundsOn(derufin));
		assertEquals(5, scn.GetFreepsHandCount());

		// An engine, a machine, a Shadow possession and a Free Peoples one; not a condition that is no engine
		// or no machine, nor an artifact.
		assertTrue(scn.FreepsHasCardChoiceAvailable(towers, ram, banner, armor));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(seeking, armory, narsil));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(ram);

		assertInZone(Zone.DISCARD, ram);
		assertInZone(Zone.SUPPORT, towers, seeking, armory);
		assertAttachedTo(banner, soldier);
		assertAttachedTo(armor, derufin);
		assertAttachedTo(narsil, derufin);
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithFourCardsInHandWhileTheShadowPlayerHasFive() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var derufin = scn.GetFreepsCard("derufin");
		scn.MoveCompanionsToTable(derufin);
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"), scn.GetFreepsCard("card4"));

		var towers = scn.GetShadowCard("towers");
		scn.MoveCardsToSupportArea(towers);
		scn.MoveCardsToHand(scn.GetShadowCard("card1"), scn.GetShadowCard("card2"), scn.GetShadowCard("card3"),
				scn.GetShadowCard("card4"), scn.GetShadowCard("card5"));

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		// "You": his own hand, one short; the other player's five do not count.
		assertEquals(4, scn.GetFreepsHandCount());
		assertEquals(5, scn.GetShadowHandCount());
		assertEquals(0, scn.GetWoundsOn(derufin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(derufin));

		// One card more, and it is.
		scn.MoveCardsToHand(scn.GetFreepsCard("card5"));
		scn.FreepsRefreshDecision();
		assertEquals(5, scn.GetFreepsHandCount());
		assertTrue(scn.FreepsActionAvailable(derufin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void RegroupAbilityIsNotOfferedWithSixCardsInHand() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var derufin = scn.GetFreepsCard("derufin");
		scn.MoveCompanionsToTable(derufin);
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"), scn.GetFreepsCard("card4"), scn.GetFreepsCard("card5"), scn.GetFreepsCard("card6"));

		var towers = scn.GetShadowCard("towers");
		scn.MoveCardsToSupportArea(towers);

		scn.StartGame();
		scn.SkipToPhase(Phase.REGROUP);

		assertEquals(6, scn.GetFreepsHandCount());
		assertEquals(0, scn.GetWoundsOn(derufin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(derufin));
	}

	@Test
	public void RegroupAbilityIsNotOfferedWhileDerufinIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var derufin = scn.GetFreepsCard("derufin");
		scn.MoveCompanionsToTable(derufin);
		scn.MoveCardsToHand(scn.GetFreepsCard("card1"), scn.GetFreepsCard("card2"), scn.GetFreepsCard("card3"), scn.GetFreepsCard("card4"), scn.GetFreepsCard("card5"));

		var towers = scn.GetShadowCard("towers");
		scn.MoveCardsToSupportArea(towers);

		scn.StartGame();
		scn.AddWoundsToChar(derufin, 2);
		scn.SkipToPhase(Phase.REGROUP);

		// Vitality 3 with 2 wounds: he cannot exert.
		assertEquals(5, scn.GetFreepsHandCount());
		assertEquals(2, scn.GetWoundsOn(derufin));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		assertFalse(scn.FreepsActionAvailable(derufin));
	}
}
