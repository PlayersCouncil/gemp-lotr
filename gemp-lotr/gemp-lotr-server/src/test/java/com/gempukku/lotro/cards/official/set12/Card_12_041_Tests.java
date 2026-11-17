package com.gempukku.lotro.cards.official.set12;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_12_041_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("toad", "12_41");

					// [gollum] cards
					put("stole", "6_46");       // They Stole It: a Shadow [gollum] support condition; a skirmish ability only
					put("larder", "8_23");      // Larder: a Shadow [gollum] support possession; regroup and skirmish abilities used only on purpose
					put("gollum", "5_24");      // Gollum, Nasty Treacherous Creature: a [gollum] minion; a regroup ability used only on purpose
					put("smeagol", "6_45");     // Smeagol, Poor Creature: a Free Peoples [gollum] companion; a skirmish ability used only on purpose
					put("dontlook", "6_39");    // Don't Look at Them: a Free Peoples [gollum] support condition; a skirmish ability only

					// Other characters
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man companion; a maneuver ability
					put("boromir", "1_96");     // Boromir, Lord of Gondor: a [gondor] Man companion; overwhelm text only
					put("bounder", "1_286");    // Bounder: a Hobbit ally, home 2F; a skirmish ability
					put("hound", "7_192");      // Morgul Hound: a [wraith] Orc minion, vitality 2
					put("runner", "1_178");     // Goblin Runner: a [moria] minion

					// The heal
					put("elrond", "1_40");      // Elrond, Lord of Rivendell: an ally of site 3, for Master of Healing to spot
					put("healing", "3_22");     // Master of Healing: Fellowship or Regroup event, "Spot Elrond ... to heal a companion or ally"
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void TreacherousLittleToadStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 12
		 * Name: Treacherous Little Toad
		 * Unique: False
		 * Side: Shadow
		 * Culture: Gollum
		 * Twilight Cost: 0
		 * Type: Condition
		 * Subtype:
		 * Game Text: To play, spot a [gollum] card.<br>Bearer must be a wounded companion.<br>While you can spot a [gollum] character or a [gollum] card in your support area, bearer cannot heal.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("toad");

		assertEquals("Treacherous Little Toad", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.GOLLUM, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void PlayableOnceAGollumCardIsSpottedAndOnlyOnAWoundedCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveAlliesToTable(bounder);

		var toad = scn.GetShadowCard("toad");
		var stole = scn.GetShadowCard("stole");
		var hound = scn.GetShadowCard("hound");
		var runner = scn.GetShadowCard("runner");
		scn.MoveCardsToHand(toad, stole);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(bounder, 1);
		scn.MoveMinionsToTable(hound, runner);
		scn.AddWoundsToChar(hound, 1);
		scn.SetTwilight(1);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(bounder));
		assertEquals(1, scn.GetWoundsOn(hound));
		scn.FreepsPass();

		// At site 2, Bounder's home: the ally is where the fellowship is.  No [gollum] card is in play.
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(toad));

		scn.ShadowPlayCard(stole);
		assertInZone(Zone.SUPPORT, stole);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(toad));
		scn.ShadowPlayCard(toad);

		// Wounded companions, the Ring-bearer among them; not an unwounded companion, a wounded ally or a wounded minion.
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, aragorn));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(boromir, bounder, hound));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aragorn);

		assertEquals(aragorn, toad.getAttachedTo());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheOtherPlayersSmeagolIsAGollumCardToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var smeagol = scn.GetFreepsCard("smeagol");
		scn.MoveCompanionsToTable(smeagol);

		var toad = scn.GetShadowCard("toad");
		var hound = scn.GetShadowCard("hound");
		scn.MoveCardsToHand(toad);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(smeagol, 1);
		scn.MoveMinionsToTable(hound);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(smeagol));
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(toad));
		scn.ShadowPlayCard(toad);
		assertTrue(scn.ShadowHasCardChoiceAvailable(frodo, smeagol));
		scn.ShadowChooseCard(smeagol);

		assertEquals(smeagol, toad.getAttachedTo());
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhileAGollumConditionIsInTheShadowSupportAreaBearerCannotHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var elrond = scn.GetFreepsCard("elrond");
		var healing = scn.GetFreepsCard("healing");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(healing);

		var toad = scn.GetShadowCard("toad");
		var stole = scn.GetShadowCard("stole");
		scn.MoveCardsToSupportArea(stole);
		scn.AttachCardsTo(aragorn, toad);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 2);
		scn.AddWoundsToChar(aragorn, 2);
		scn.AddWoundsToChar(boromir, 1);
		scn.FreepsRefreshDecision();
		assertEquals(aragorn, toad.getAttachedTo());
		assertInZone(Zone.SUPPORT, stole);
		assertEquals(2, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));

		assertTrue(scn.FreepsPlayAvailable(healing));
		scn.FreepsPlayCard(healing);

		// The other wounded companions may be healed; the bearer may not.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		scn.FreepsChooseCard(frodo);

		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhileAGollumPossessionIsInTheShadowSupportAreaBearerCannotHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var elrond = scn.GetFreepsCard("elrond");
		var healing = scn.GetFreepsCard("healing");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(healing);

		var toad = scn.GetShadowCard("toad");
		var larder = scn.GetShadowCard("larder");
		scn.MoveCardsToSupportArea(larder);
		scn.AttachCardsTo(aragorn, toad);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(boromir, 1);
		scn.FreepsRefreshDecision();
		assertEquals(aragorn, toad.getAttachedTo());
		assertInZone(Zone.SUPPORT, larder);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));

		scn.FreepsPlayCard(healing);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		scn.FreepsChooseCard(boromir);

		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhileTheOtherPlayersSmeagolIsInPlayBearerCannotHeal() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var smeagol = scn.GetFreepsCard("smeagol");
		var elrond = scn.GetFreepsCard("elrond");
		var healing = scn.GetFreepsCard("healing");
		scn.MoveCompanionsToTable(aragorn, smeagol);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(healing);

		var toad = scn.GetShadowCard("toad");
		scn.AttachCardsTo(aragorn, toad);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(smeagol, 1);
		scn.FreepsRefreshDecision();
		assertEquals(aragorn, toad.getAttachedTo());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(smeagol));

		scn.FreepsPlayCard(healing);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, smeagol));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		scn.FreepsChooseCard(smeagol);

		assertEquals(0, scn.GetWoundsOn(smeagol));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void WhileGollumIsInPlayBearerCannotHealInTheRegroupPhase() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var elrond = scn.GetFreepsCard("elrond");
		var healing = scn.GetFreepsCard("healing");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToHand(healing);

		var toad = scn.GetShadowCard("toad");
		var gollum = scn.GetShadowCard("gollum");
		scn.AttachCardsTo(aragorn, toad);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(boromir, 1);
		assertEquals(aragorn, toad.getAttachedTo());
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));
		scn.SkipToPhase(Phase.REGROUP);
		// Gollum stands where minions that lived through the skirmishes stand.
		scn.MoveMinionsToTable(gollum);
		scn.FreepsRefreshDecision();

		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
		scn.FreepsPlayCard(healing);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, boromir));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(aragorn));
		scn.FreepsChooseCard(boromir);

		assertEquals(0, scn.GetWoundsOn(boromir));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowRegroupPhaseActions());
	}

	@Test
	public void WithNoGollumCharacterAndNoGollumCardInTheShadowSupportAreaBearerHeals() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var elrond = scn.GetFreepsCard("elrond");
		var healing = scn.GetFreepsCard("healing");
		var dontlook = scn.GetFreepsCard("dontlook");
		scn.MoveCompanionsToTable(aragorn, boromir);
		scn.MoveAlliesToTable(elrond);
		scn.MoveCardsToSupportArea(dontlook);
		scn.MoveCardsToHand(healing);

		// The [gollum] cards in play: the Toad itself, borne by a companion, and the Free Peoples player's
		// Don't Look at Them in his own support area.
		var toad = scn.GetShadowCard("toad");
		scn.AttachCardsTo(aragorn, toad);

		scn.StartGame();
		scn.AddWoundsToChar(frodo, 1);
		scn.AddWoundsToChar(aragorn, 1);
		scn.AddWoundsToChar(boromir, 1);
		scn.FreepsRefreshDecision();
		assertEquals(aragorn, toad.getAttachedTo());
		assertInZone(Zone.SUPPORT, dontlook);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(1, scn.GetWoundsOn(boromir));

		scn.FreepsPlayCard(healing);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, aragorn, boromir));
		scn.FreepsChooseCard(aragorn);

		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(aragorn, toad.getAttachedTo());
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
