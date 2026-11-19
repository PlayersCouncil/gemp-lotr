package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_051_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stampeded", "10_51");
					// Men of Harad: a [raider] condition in the support area; its ability is not used
					put("harad", "4_238");
					// Black Sails of Umbar: a [raider] possession in the support area; its abilities are not used
					put("sails", "8_50");
					// Southron Wanderer: a [raider] Man minion, strength 8; his assignment ability needs 5 companions
					put("wanderer", "4_258");
					// Raider Halberd: a Shadow possession, cost 0, borne by a [raider] Man; a skirmish ability only
					put("halberd", "7_156");
					// Teeth of Mordor: a [sauron] condition in the support area; its abilities are not used
					put("teeth", "5_110");

					put("knight", "5_35");      // Gondorian Knight: an unbound [gondor] Man, vitality 3; Knight only
					put("guard", "5_83");       // Household Guard: an unbound [rohan] Man, vitality 3; Valiant only
					// Gondorian Ranger: a Ring-bound [gondor] Man; his text is about minions assigned to him
					put("ranger", "4_122");
					// Gondorian Merchant: a [gondor] Man who is an ally; a maneuver ability that needs initiative
					put("merchant", "7_97");
					put("gimli", "0_62");       // Gimli, Dwarven Delegate: an unbound companion who is no Man; damage +1 only
					put("bounder", "1_286");    // Bounder: a Hobbit ally; a skirmish ability only
					// Éowyn, Daughter of Éomund: "Maneuver: Play a [rohan] possession on Éowyn to heal her."
					put("eowyn", "5_122");

					put("armor", "1_8");        // Dwarven Armor: [dwarven] possession, cost 0; bearer must be a Dwarf
					put("sword", "1_299");      // Hobbit Sword: [shire] possession, cost 1; bearer must be a Hobbit
					put("gsword", "4_134");     // Sword of Gondor: [gondor] possession, cost 1; bearer must be a [gondor] Man
					put("bow", "4_131");        // Ranger's Bow: [gondor] possession, cost 1; bearer must be a Ring-bound Man
					put("spear", "4_288");      // Rider's Spear: [rohan] possession, cost 1; bearer must be a [rohan] Man
					put("ring", "9_8");         // Ring of Guile: a [dwarven] ARTIFACT a Dwarf bears, cost 0
					put("saga", "1_114");       // The Saga of Elendil: a [gondor] CONDITION a [gondor] companion bears, cost 1
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StampededStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Stampeded
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 0
		 * Type: Event
		 * Subtype: Response
		 * Game Text: If a Free Peoples possession is played, spot a [raider] card to discard that possession. If that
		 * possession was a [gondor] or [rohan] possession, wound an unbound Man.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stampeded");

		assertEquals("Stampeded", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.RESPONSE));
		assertEquals(0, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ADwarvenPossessionPlayedIsDiscardedAndNoManIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var knight = scn.GetFreepsCard("knight");
		var ranger = scn.GetFreepsCard("ranger");
		var bow = scn.GetFreepsCard("bow");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli, knight, ranger);
		scn.AttachCardsTo(ranger, bow);
		scn.MoveCardsToHand(armor);

		var stampeded = scn.GetShadowCard("stampeded");
		var harad = scn.GetShadowCard("harad");
		scn.MoveCardsToHand(stampeded);
		scn.MoveCardsToSupportArea(harad);

		scn.StartGame();

		// A [gondor] possession already in play, and an unbound Man to wound.
		assertAttachedTo(bow, ranger);
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(gimli));

		scn.FreepsPlayCard(armor);
		assertAttachedTo(armor, gimli);
		assertTrue(scn.ShadowPlayAvailable(stampeded));
		scn.ShadowPlayCard(stampeded);

		// The Armor played is discarded, not the Bow; it was [dwarven], so nobody is wounded and nothing is asked.
		assertInDiscard(armor, stampeded);
		assertAttachedTo(bow, ranger);
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(ranger));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AShirePossessionPlayedOnAnAllyIsDiscardedBySpottingARaiderPossession() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var bounder = scn.GetFreepsCard("bounder");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(sword);

		var stampeded = scn.GetShadowCard("stampeded");
		var sails = scn.GetShadowCard("sails");
		scn.MoveCardsToHand(stampeded);
		scn.MoveCardsToSupportArea(sails);

		scn.StartGame();

		// The only [raider] card in play is a possession.
		assertInZone(Zone.SUPPORT, sails);
		scn.FreepsPlayCard(sword);
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, bounder));
		scn.FreepsChooseCard(bounder);
		assertAttachedTo(sword, bounder);

		assertTrue(scn.ShadowPlayAvailable(stampeded));
		scn.ShadowPlayCard(stampeded);
		assertInDiscard(sword, stampeded);
		assertInZone(Zone.SUPPORT, bounder, sails);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void AnArtifactOrAConditionPlayedIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var knight = scn.GetFreepsCard("knight");
		var ring = scn.GetFreepsCard("ring");
		var saga = scn.GetFreepsCard("saga");
		var armor = scn.GetFreepsCard("armor");
		scn.MoveCompanionsToTable(gimli, knight);
		scn.MoveCardsToHand(ring, saga, armor);

		var stampeded = scn.GetShadowCard("stampeded");
		var harad = scn.GetShadowCard("harad");
		scn.MoveCardsToHand(stampeded);
		scn.MoveCardsToSupportArea(harad);
		assertInHand(stampeded);

		scn.StartGame();

		// A Free Peoples artifact.
		scn.FreepsPlayCard(ring);
		assertAttachedTo(ring, gimli);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A Free Peoples condition.
		scn.FreepsPlayCard(saga);
		assertAttachedTo(saga, knight);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		// A Free Peoples possession, on the same board.
		scn.FreepsPlayCard(armor);
		assertAttachedTo(armor, gimli);
		assertInHand(stampeded);
		assertTrue(scn.ShadowPlayAvailable(stampeded));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void IsNotOfferedUntilARaiderCardIsInPlay() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(armor, sword);

		var stampeded = scn.GetShadowCard("stampeded");
		var harad = scn.GetShadowCard("harad");
		var teeth = scn.GetShadowCard("teeth");
		scn.MoveCardsToHand(stampeded, harad);
		scn.MoveCardsToSupportArea(teeth);

		scn.StartGame();

		// A Shadow condition of another culture in play, and a [raider] card in hand.
		assertInZone(Zone.SUPPORT, teeth);
		assertInHand(harad);
		scn.FreepsPlayCard(armor);
		assertAttachedTo(armor, gimli);
		assertFalse(scn.ShadowAnyDecisionsAvailable());
		assertTrue(scn.AwaitingFellowshipPhaseActions());

		scn.MoveCardsToSupportArea(harad);
		assertInZone(Zone.SUPPORT, harad);
		scn.FreepsRefreshDecision();
		scn.FreepsPlayCard(sword);
		assertAttachedTo(sword, frodo);
		assertTrue(scn.ShadowPlayAvailable(stampeded));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable());
	}

	@Test
	public void AShadowPossessionPlayedIsNotAnswered() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stampeded = scn.GetShadowCard("stampeded");
		var wanderer = scn.GetShadowCard("wanderer");
		var halberd = scn.GetShadowCard("halberd");
		scn.MoveCardsToHand(stampeded, halberd);
		assertInHand(stampeded, halberd);

		scn.StartGame();
		scn.MoveMinionsToTable(wanderer);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowPhaseActions());
		scn.ShadowPlayCard(halberd);
		assertAttachedTo(halberd, wanderer);
		// No response is asked: the Shadow phase goes on.
		assertInHand(stampeded);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void AGondorPossessionPlayedIsDiscardedAndAnUnboundManIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var knight = scn.GetFreepsCard("knight");
		var guard = scn.GetFreepsCard("guard");
		var ranger = scn.GetFreepsCard("ranger");
		var gimli = scn.GetFreepsCard("gimli");
		var merchant = scn.GetFreepsCard("merchant");
		var gsword = scn.GetFreepsCard("gsword");
		scn.MoveCompanionsToTable(knight, guard, ranger, gimli);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(gsword);

		var stampeded = scn.GetShadowCard("stampeded");
		var harad = scn.GetShadowCard("harad");
		scn.MoveCardsToHand(stampeded);
		scn.MoveCardsToSupportArea(harad);

		scn.StartGame();

		scn.FreepsPlayCard(gsword);
		assertTrue(scn.FreepsHasCardChoiceAvailable(knight, ranger, merchant));
		scn.FreepsChooseCard(knight);
		assertAttachedTo(gsword, knight);

		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(guard));
		scn.ShadowPlayCard(stampeded);
		assertInDiscard(gsword);

		// The unbound Men of either culture; not the Ring-bound Man, the Man who is an ally, the unbound Dwarf or
		// the Hobbit.
		assertTrue(scn.ShadowHasCardChoiceAvailable(knight, guard));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(ranger, merchant, gimli, frodo));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(guard);

		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(0, scn.GetWoundsOn(knight));
		assertEquals(0, scn.GetWoundsOn(ranger));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertInDiscard(stampeded);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void ARohanPossessionPlayedInTheManeuverPhaseIsDiscardedAndTheWoundKillsAnExhaustedMan() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var eowyn = scn.GetFreepsCard("eowyn");
		var knight = scn.GetFreepsCard("knight");
		var spear = scn.GetFreepsCard("spear");
		scn.MoveCompanionsToTable(eowyn, knight);
		scn.MoveCardsToHand(spear);
		assertInHand(spear);

		var stampeded = scn.GetShadowCard("stampeded");
		var wanderer = scn.GetShadowCard("wanderer");
		scn.MoveCardsToHand(stampeded);

		scn.StartGame();
		scn.AddWoundsToChar(eowyn, 1);
		// Vitality 3: exhausted.
		scn.AddWoundsToChar(knight, 2);
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertEquals(2, scn.GetWoundsOn(knight));
		scn.MoveMinionsToTable(wanderer);
		scn.SkipToPhase(Phase.MANEUVER);

		// The only [raider] card in play is a minion.
		assertInZone(Zone.SHADOW_CHARACTERS, wanderer);
		scn.FreepsUseCardAction(eowyn);
		assertAttachedTo(spear, eowyn);
		assertTrue(scn.ShadowPlayAvailable(stampeded));
		scn.ShadowPlayCard(stampeded);
		assertInDiscard(spear);

		// The unbound Men: Éowyn and the exhausted Knight; the Southron, a Man who is no companion, is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(eowyn, knight));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(wanderer));
		scn.ShadowChooseCard(knight);

		// A wound, not an exertion: the exhausted Knight is killed.
		assertInZone(Zone.DEAD, knight);
		assertEquals(0, scn.GetWoundsOn(wanderer));
		assertInDiscard(stampeded);
		// The Spear left play before its play was complete, so Éowyn's ability, whose cost it was, heals nobody
		// (pitfalls, "A play as a cost can come to nothing").
		assertEquals(1, scn.GetWoundsOn(eowyn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void AGondorPossessionPlayedWithNoUnboundManInPlayIsDiscardedAndNobodyIsWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var frodo = scn.GetRingBearer();
		var ranger = scn.GetFreepsCard("ranger");
		var gimli = scn.GetFreepsCard("gimli");
		var merchant = scn.GetFreepsCard("merchant");
		var bow = scn.GetFreepsCard("bow");
		scn.MoveCompanionsToTable(ranger, gimli);
		scn.MoveAlliesToTable(merchant);
		scn.MoveCardsToHand(bow);

		var stampeded = scn.GetShadowCard("stampeded");
		var harad = scn.GetShadowCard("harad");
		scn.MoveCardsToHand(stampeded);
		scn.MoveCardsToSupportArea(harad);

		scn.StartGame();

		scn.FreepsPlayCard(bow);
		assertAttachedTo(bow, ranger);
		assertTrue(scn.ShadowPlayAvailable(stampeded));
		scn.ShadowPlayCard(stampeded);

		// A Ring-bound Man, an ally who is a Man, an unbound Dwarf and the Hobbit: nobody to wound, and nothing asked.
		assertInDiscard(bow, stampeded);
		assertEquals(0, scn.GetWoundsOn(ranger));
		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(merchant));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}
}
