package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_020_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("turn", "5_20");

					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: vitality 4; a strength bonus only
					put("avenger", "6_27");     // Ent Avenger: a [gandalf] companion who is no Gandalf; a strength bonus only beside 3 Ents
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man; a maneuver ability used only on purpose
					put("armor", "1_92");       // Armor: a Free Peoples possession on a Man; caps his wounds in skirmishes only
					put("defend", "4_115");     // Defend It and Hope: Maneuver, exert a [gondor] Man to wound a minion once (twice an Uruk-hai)

					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, no text
					put("trooper", "3_99");     // Orc Trooper: [sauron] Orc, strength +1 for each card in the Shadow hand
					put("runner", "1_178");     // Goblin Runner: [moria] Orc, vitality 1; its play text is not reached cheated in
					put("scimitar", "1_180");   // Goblin Scimitar: [moria] possession on a [moria] Orc; its play text is not reached cheated in
					put("banner", "6_98");      // Banner of the Eye: [sauron] possession on a [sauron] Orc; a trigger on a skirmish won
					put("poison", "8_28");      // Spider Poison: [gollum] possession in the Shadow support area; a skirmish ability about Shelob
					put("stone", "9_47");       // Ithil Stone: a [sauron] artifact, no possession; an optional Shadow-phase draw and a Shadow ability
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TurnoftheTideStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Turn of the Tide
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Gandalf
		 * Twilight Cost: 3
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Spell</b>.<br><b>Maneuver:</b> Exert Gandalf to discard up to 2 Shadow possessions. Any Shadow player may discard a minion to prevent this.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("turn");

		assertEquals("Turn of the Tide", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.GANDALF, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SPELL));
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsGandalfToDiscardUpToTwoShadowPossessionsWhenTheShadowPlayerDoesNotPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var turn = scn.GetFreepsCard("turn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(aragorn, armor);
		scn.MoveCardsToHand(turn);

		var troop = scn.GetShadowCard("troop");
		var trooper = scn.GetShadowCard("trooper");
		var scimitar = scn.GetShadowCard("scimitar");
		var banner = scn.GetShadowCard("banner");
		var poison = scn.GetShadowCard("poison");
		var stone = scn.GetShadowCard("stone");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, trooper);
		scn.AttachCardsTo(troop, scimitar);
		scn.AttachCardsTo(trooper, banner);
		scn.MoveCardsToSupportArea(poison, stone);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.HAND, turn);
		assertInZone(Zone.ATTACHED, scimitar, banner, armor);
		assertInZone(Zone.SUPPORT, poison, stone);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsPlayAvailable(turn));
		scn.FreepsPlayCard(turn);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		// The Shadow player is asked first, and declines.
		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseNo();

		// Shadow possessions of three cultures, borne and in the support area, are offered; a Shadow artifact and a
		// Free Peoples possession are not.  Up to 2: none to two.
		assertTrue(scn.FreepsHasCardChoiceAvailable(scimitar, banner, poison));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(stone, armor));
		assertEquals(0, scn.FreepsGetChoiceMin());
		assertEquals(2, scn.FreepsGetChoiceMax());
		scn.FreepsChooseCards(scimitar, poison);

		assertInDiscard(scimitar, poison);
		assertInZone(Zone.ATTACHED, banner, armor);
		assertInZone(Zone.SUPPORT, stone);
		assertInZone(Zone.SHADOW_CHARACTERS, troop, trooper);
		assertInDiscard(turn);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void TheShadowPlayerMayDiscardAMinionToPreventTheDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var turn = scn.GetFreepsCard("turn");
		var gandalf = scn.GetFreepsCard("gandalf");
		scn.MoveCompanionsToTable(gandalf);
		scn.MoveCardsToHand(turn);

		var troop = scn.GetShadowCard("troop");
		var trooper = scn.GetShadowCard("trooper");
		var runner = scn.GetShadowCard("runner");
		var scimitar = scn.GetShadowCard("scimitar");
		var banner = scn.GetShadowCard("banner");
		var poison = scn.GetShadowCard("poison");
		var stone = scn.GetShadowCard("stone");

		scn.StartGame();
		scn.MoveMinionsToTable(troop, trooper, runner);
		scn.AttachCardsTo(troop, scimitar);
		scn.AttachCardsTo(trooper, banner);
		scn.MoveCardsToSupportArea(poison, stone);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.HAND, turn);
		assertInZone(Zone.ATTACHED, scimitar, banner);
		assertInZone(Zone.SUPPORT, poison, stone);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.FreepsPlayCard(turn);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertTrue(scn.ShadowDecisionAvailable("prevent"));
		scn.ShadowChooseYes();

		// Minions of two cultures are offered; the Shadow possession and artifact are not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(troop, trooper, runner));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(poison, stone, scimitar));
		scn.ShadowChooseCard(runner);

		// The minion is discarded, and no possession: Gandalf's exertion stays paid.
		assertInDiscard(runner);
		assertInZone(Zone.SHADOW_CHARACTERS, troop, trooper);
		assertInZone(Zone.ATTACHED, scimitar, banner);
		assertInZone(Zone.SUPPORT, poison, stone);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertInDiscard(turn);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithGandalfExhaustedItCannotBePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var turn = scn.GetFreepsCard("turn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var avenger = scn.GetFreepsCard("avenger");
		scn.MoveCompanionsToTable(gandalf, avenger);
		scn.MoveCardsToHand(turn);
		// Vitality 4: exhausted.  The [gandalf] Ent beside him could exert, but is no Gandalf.
		scn.AddWoundsToChar(gandalf, 3);

		var troop = scn.GetShadowCard("troop");
		var poison = scn.GetShadowCard("poison");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToSupportArea(poison);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.HAND, turn);
		assertInZone(Zone.SUPPORT, poison);
		assertEquals(3, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(avenger));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
		assertFalse(scn.FreepsPlayAvailable(turn));
	}

	@Test
	public void WithNoShadowPossessionItIsPlayedForNothingAndThePreventionIsNotAsked() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var turn = scn.GetFreepsCard("turn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.AttachCardsTo(aragorn, armor);
		scn.MoveCardsToHand(turn);

		var troop = scn.GetShadowCard("troop");
		var stone = scn.GetShadowCard("stone");

		scn.StartGame();
		scn.MoveMinionsToTable(troop);
		scn.MoveCardsToSupportArea(stone);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.HAND, turn);
		assertInZone(Zone.ATTACHED, armor);
		assertInZone(Zone.SUPPORT, stone);
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertTrue(scn.FreepsPlayAvailable(turn));
		scn.FreepsPlayCard(turn);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertInZone(Zone.SHADOW_CHARACTERS, troop);
		assertInZone(Zone.ATTACHED, armor);
		assertInZone(Zone.SUPPORT, stone);
		assertInDiscard(turn);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void WithNoMinionLeftTheShadowPlayerCannotPreventAndSpiderPoisonIsDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var turn = scn.GetFreepsCard("turn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var aragorn = scn.GetFreepsCard("aragorn");
		var defend = scn.GetFreepsCard("defend");
		scn.MoveCompanionsToTable(gandalf, aragorn);
		scn.MoveCardsToHand(turn, defend);

		var runner = scn.GetShadowCard("runner");
		var poison = scn.GetShadowCard("poison");

		scn.StartGame();
		scn.MoveMinionsToTable(runner);
		scn.MoveCardsToSupportArea(poison);
		scn.SkipToPhase(Phase.MANEUVER);

		assertInZone(Zone.HAND, turn, defend);
		assertInZone(Zone.SUPPORT, poison);
		// The only minion is killed first, by another maneuver event.
		scn.FreepsPlayCard(defend);
		assertInDiscard(runner);
		scn.ShadowPass();

		assertEquals(0, scn.GetWoundsOn(gandalf));
		scn.FreepsPlayCard(turn);

		assertEquals(1, scn.GetWoundsOn(gandalf));
		// No minion to discard: the Shadow player cannot pay, and is not asked.
		assertFalse(scn.ShadowDecisionAvailable("prevent"));
		assertTrue(scn.FreepsHasCardChoiceAvailable(poison));
		scn.FreepsChooseCard(poison);

		assertInDiscard(poison, turn);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}
}
