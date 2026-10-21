package com.gempukku.lotro.cards.official.set02;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_080_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("dumb", "2_80");

					// The Witch-king, Lord of Angmar: fierce; strength +2 for each other Nazgûl, nothing else
					put("witchking", "1_237");
					// Úlairë Nelya, Ringwraith in Twilight: another [wraith] Nazgûl, able to exert; her text needs 2
					// burdens or 2 wounds on the Ring-bearer
					put("nelya", "2_84");
					// The Pale Blade: the Shadow player's own [wraith] hand weapon, a possession, on The Witch-king
					put("blade", "1_221");
					// Black Steed: a [wraith] possession that is no weapon (a mount); +2 at a plains site, which site 2
					// is, and discarded only at an underground site
					put("steed", "1_208");

					// Aragorn, Ranger of the North: a [gondor] Man to bear the Saga, Narsil and his Bow; his maneuver
					// ability is never used here
					put("aragorn", "1_89");
					// Gimli, Son of Glóin: a Dwarf to bear the Axe and the Armor; his skirmish ability is never used
					put("gimli", "1_13");

					// The Saga of Elendil: a [gondor] tale that is a condition, on a [gondor] companion
					put("saga", "1_114");
					// Red Book of Westmarch: a [shire] tale that is a possession, in the support area; its trigger
					// needs Bilbo
					put("redbook", "2_113");
					// Scroll of Isildur: a [gondor] tale that is an artifact, in the support area
					put("scroll", "9_36");
					// Narsil, Blade of the Faithful: an artifact hand weapon
					put("narsil", "9_34");
					// Dwarven Axe: a [dwarven] possession hand weapon
					put("axe", "1_9");
					// Aragorn's Bow: a ranged weapon
					put("bow", "1_90");
					// Dwarven Armor: a possession that is no weapon (armor)
					put("armor", "1_8");
					// Lord of Moria: a Free Peoples condition that is no tale; each Dwarf is damage +1
					put("lord", "1_21");
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void StrickenDumbStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Stricken Dumb
		 * Unique: False
		 * Side: Shadow
		 * Culture: Ringwraith
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: <b>Maneuver:</b> Exert The Witch-king to discard a tale or weapon.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("dumb");

		assertEquals("Stricken Dumb", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.WRAITH, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsTheWitchKingToDiscardATaleOrAWeaponOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dumb = scn.GetShadowCard("dumb");
		var witchking = scn.GetShadowCard("witchking");
		var blade = scn.GetShadowCard("blade");
		var steed = scn.GetShadowCard("steed");
		var aragorn = scn.GetFreepsCard("aragorn");
		var gimli = scn.GetFreepsCard("gimli");
		var saga = scn.GetFreepsCard("saga");
		var redbook = scn.GetFreepsCard("redbook");
		var scroll = scn.GetFreepsCard("scroll");
		var narsil = scn.GetFreepsCard("narsil");
		var axe = scn.GetFreepsCard("axe");
		var bow = scn.GetFreepsCard("bow");
		var armor = scn.GetFreepsCard("armor");
		var lord = scn.GetFreepsCard("lord");
		scn.MoveCardsToHand(dumb);
		scn.MoveCompanionsToTable(aragorn, gimli);
		scn.AttachCardsTo(aragorn, saga, narsil, bow);
		scn.AttachCardsTo(gimli, axe, armor);
		scn.MoveCardsToSupportArea(redbook, scroll, lord);
		assertAttachedTo(saga, aragorn);
		assertAttachedTo(narsil, aragorn);
		assertAttachedTo(bow, aragorn);
		assertAttachedTo(axe, gimli);
		assertAttachedTo(armor, gimli);
		assertInZone(Zone.SUPPORT, redbook, scroll, lord);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);
		scn.AttachCardsTo(witchking, blade, steed);
		assertAttachedTo(blade, witchking);
		assertAttachedTo(steed, witchking);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(witchking));
		assertTrue(scn.ShadowPlayAvailable(dumb));

		scn.ShadowPlayCard(dumb);

		// The Witch-king, the only one, exerts unasked.  Every tale and every weapon is offered, of whatever type,
		// class, culture or side, the Shadow player's own Pale Blade among them; the armor, the mount and the
		// condition that is no tale are not.
		assertEquals(1, scn.GetWoundsOn(witchking));
		assertEquals(7, scn.ShadowGetCardChoiceCount());
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(saga, redbook, scroll, narsil, axe, bow, blade));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(armor, steed, lord));
		scn.ShadowChooseCard(narsil);

		assertInDiscard(narsil);
		assertAttachedTo(saga, aragorn);
		assertAttachedTo(bow, aragorn);
		assertAttachedTo(axe, gimli);
		assertAttachedTo(armor, gimli);
		assertAttachedTo(blade, witchking);
		assertAttachedTo(steed, witchking);
		assertInZone(Zone.SUPPORT, redbook, scroll, lord);
		assertInDiscard(dumb);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhileTheWitchKingIsExhaustedThoughAnotherNazgulCouldExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dumb = scn.GetShadowCard("dumb");
		var witchking = scn.GetShadowCard("witchking");
		var nelya = scn.GetShadowCard("nelya");
		var aragorn = scn.GetFreepsCard("aragorn");
		var saga = scn.GetFreepsCard("saga");
		scn.MoveCardsToHand(dumb);
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, saga);
		assertAttachedTo(saga, aragorn);
		assertInHand(dumb);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking, nelya);
		// Vitality 4 with 3 wounds: exhausted.  Nelya could exert, but is no Witch-king.
		scn.AddWoundsToChar(witchking, 3);
		assertEquals(3, scn.GetWoundsOn(witchking));
		assertEquals(0, scn.GetWoundsOn(nelya));

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(dumb));
		assertEquals(0, scn.GetWoundsOn(nelya));
		assertAttachedTo(saga, aragorn);
		assertInHand(dumb);
	}

	@Test
	public void WithNoTaleOrWeaponInPlayTheWitchKingExertsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var dumb = scn.GetShadowCard("dumb");
		var witchking = scn.GetShadowCard("witchking");
		var steed = scn.GetShadowCard("steed");
		var gimli = scn.GetFreepsCard("gimli");
		var armor = scn.GetFreepsCard("armor");
		var lord = scn.GetFreepsCard("lord");
		scn.MoveCardsToHand(dumb);
		scn.MoveCompanionsToTable(gimli);
		scn.AttachCardsTo(gimli, armor);
		scn.MoveCardsToSupportArea(lord);
		assertAttachedTo(armor, gimli);
		assertInZone(Zone.SUPPORT, lord);

		scn.StartGame();
		scn.MoveMinionsToTable(witchking);
		scn.AttachCardsTo(witchking, steed);
		assertAttachedTo(steed, witchking);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(0, scn.GetWoundsOn(witchking));
		// The discard is the event's effect, not a requirement: with nothing it could discard, it is still played.
		assertTrue(scn.ShadowPlayAvailable(dumb));

		scn.ShadowPlayCard(dumb);

		assertEquals(1, scn.GetWoundsOn(witchking));
		assertAttachedTo(armor, gimli);
		assertAttachedTo(steed, witchking);
		assertInZone(Zone.SUPPORT, lord);
		assertInDiscard(dumb);
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}
}
