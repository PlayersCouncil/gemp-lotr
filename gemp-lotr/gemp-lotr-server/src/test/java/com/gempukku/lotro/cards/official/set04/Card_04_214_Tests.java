package com.gempukku.lotro.cards.official.set04;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_04_214_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("stowed", "4_214");
					// Gríma, Wormtongue: the [isengard] Gríma, a Man minion; his maneuver ability needs a companion
					// bearing 3 or more cards, and none does.
					put("grima", "4_154");
					// Gríma, Witless Worm: the [men] Gríma, a Man minion; three regroup abilities, never used.
					put("mengrima", "18_67");
					// Unferth, Gríma's Bodyguard: an [isengard] Man minion who is no Gríma, though his title holds the
					// name; his text answers assignments only.
					put("unferth", "4_178");
					put("savage", "1_151");     // Uruk Savage: an [isengard] Uruk-hai, damage +1 and nothing else
					// Uruk-hai Sword: an [isengard] possession, "Bearer must be an Uruk-hai"; its text answers a
					// skirmish won.
					put("uruksword", "1_160");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: unbound; a maneuver ability never used
					// Ring of Barahir: a [gondor] artifact, "Bearer must be Aragorn"; an optional start-of-turn heal,
					// so attached after the game starts.
					put("barahir", "6_55");
					put("gimli", "1_13");       // Gimli, Son of Glóin: unbound; a skirmish ability never used
					// Dwarven Axe: a [dwarven] possession, "Bearer must be a Dwarf"; its text answers a skirmish lost
					// to bearer.
					put("axe", "1_9");
					put("sam", "1_311");        // Sam, Son of Hamfast: Ring-bound and not the Ring-bearer
					// Farmer Maggot, Chaser of Rascals: a [shire] Hobbit ally; a fellowship ability never used.
					put("maggot", "1_288");
					put("sword1", "1_299");     // Hobbit Sword: a [shire] possession, "Bearer must be a Hobbit", no other text
					put("sword2", "1_299");
					// Old Toby: a [shire] possession in the support area, borne by nobody; its draw answers its own
					// play only.
					put("toby", "1_305");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void WhereHasGrimaStowedItStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 4
		 * Name: Where Has Gríma Stowed It?
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Spot Gríma to discard a possession borne by an ally or unbound companion.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("stowed");

		assertEquals("Where Has Gríma Stowed It?", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithGrimaSpottedItDiscardsAPossessionBorneByAnAllyOrUnboundCompanion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stowed = scn.GetShadowCard("stowed");
		var grima = scn.GetShadowCard("grima");
		var savage = scn.GetShadowCard("savage");
		var uruksword = scn.GetShadowCard("uruksword");
		var aragorn = scn.GetFreepsCard("aragorn");
		var barahir = scn.GetFreepsCard("barahir");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var sam = scn.GetFreepsCard("sam");
		var maggot = scn.GetFreepsCard("maggot");
		var sword1 = scn.GetFreepsCard("sword1");
		var sword2 = scn.GetFreepsCard("sword2");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(aragorn, gimli, sam);
		scn.MoveAlliesToTable(maggot);
		scn.MoveCardsToSupportArea(toby);
		scn.MoveCardsToHand(stowed);
		assertInZone(Zone.SUPPORT, toby);
		assertInZone(Zone.HAND, stowed);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, barahir);
		scn.AttachCardsTo(gimli, axe);
		scn.AttachCardsTo(sam, sword1);
		scn.AttachCardsTo(maggot, sword2);
		assertAttachedTo(barahir, aragorn);
		assertAttachedTo(axe, gimli);
		assertAttachedTo(sword1, sam);
		assertAttachedTo(sword2, maggot);
		scn.MoveMinionsToTable(grima, savage);
		scn.AttachCardsTo(savage, uruksword);
		assertAttachedTo(uruksword, savage);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(stowed));
		scn.ShadowPlayCard(stowed);

		// A possession borne by an ally and one borne by an unbound companion, of two cultures.  Not one borne by
		// Sam (Ring-bound), an artifact borne by an unbound companion, a possession borne by nobody, nor one
		// borne by a minion.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword2, axe));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sword1, barahir, toby, uruksword));
		scn.ShadowChooseCard(sword2);

		assertInZone(Zone.DISCARD, sword2, stowed);
		assertAttachedTo(axe, gimli);
		assertAttachedTo(sword1, sam);
		assertAttachedTo(barahir, aragorn);
		assertAttachedTo(uruksword, savage);
		assertInZone(Zone.SUPPORT, toby);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWithAnIsengardManWhoIsNoGrima() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stowed = scn.GetShadowCard("stowed");
		var unferth = scn.GetShadowCard("unferth");
		var gimli = scn.GetFreepsCard("gimli");
		var axe = scn.GetFreepsCard("axe");
		var maggot = scn.GetFreepsCard("maggot");
		var sword2 = scn.GetFreepsCard("sword2");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveAlliesToTable(maggot);
		scn.MoveCardsToHand(stowed);
		assertInZone(Zone.HAND, stowed);

		scn.StartGame();
		scn.AttachCardsTo(gimli, axe);
		scn.AttachCardsTo(maggot, sword2);
		assertAttachedTo(axe, gimli);
		assertAttachedTo(sword2, maggot);
		scn.MoveMinionsToTable(unferth);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Unferth's title holds the name and is not Gríma; there are possessions to discard and the pool can pay.
		assertTrue(scn.GetTwilight() >= 2);
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertInZone(Zone.HAND, stowed);
		assertFalse(scn.ShadowPlayAvailable(stowed));
		assertAttachedTo(axe, gimli);
		assertAttachedTo(sword2, maggot);
	}

	@Test
	public void WithTheMenGrimaAndNoPossessionBorneByAnAllyOrUnboundCompanionItIsPlayedForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var stowed = scn.GetShadowCard("stowed");
		var mengrima = scn.GetShadowCard("mengrima");
		var savage = scn.GetShadowCard("savage");
		var uruksword = scn.GetShadowCard("uruksword");
		var aragorn = scn.GetFreepsCard("aragorn");
		var barahir = scn.GetFreepsCard("barahir");
		var sam = scn.GetFreepsCard("sam");
		var sword1 = scn.GetFreepsCard("sword1");
		var toby = scn.GetFreepsCard("toby");
		scn.MoveCompanionsToTable(aragorn, sam);
		scn.MoveCardsToSupportArea(toby);
		scn.MoveCardsToHand(stowed);
		assertInZone(Zone.SUPPORT, toby);
		assertInZone(Zone.HAND, stowed);

		scn.StartGame();
		scn.AttachCardsTo(aragorn, barahir);
		scn.AttachCardsTo(sam, sword1);
		assertAttachedTo(barahir, aragorn);
		assertAttachedTo(sword1, sam);
		scn.MoveMinionsToTable(mengrima, savage);
		scn.AttachCardsTo(savage, uruksword);
		assertAttachedTo(uruksword, savage);
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// The [men] Gríma is Gríma too.  The event's target is no requirement (F-0665): played, nothing discarded.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertTrue(scn.ShadowPlayAvailable(stowed));
		scn.ShadowPlayCard(stowed);

		assertInZone(Zone.DISCARD, stowed);
		assertAttachedTo(sword1, sam);
		assertAttachedTo(barahir, aragorn);
		assertAttachedTo(uruksword, savage);
		assertInZone(Zone.SUPPORT, toby);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
