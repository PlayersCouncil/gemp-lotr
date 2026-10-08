package com.gempukku.lotro.cards.unofficial.pc.errata.set02;

import com.gempukku.lotro.cards.build.InvalidCardDefinitionException;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.IsAdditionalRaceModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_02_039_ErrataTests
{
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("beyond", "52_39");

					put("warrior", "4_165");    // Orthanc Warrior: [isengard] Uruk-hai, vitality 2, damage +1 and nothing else
					put("whu", "18_131");       // White Hand Uruk: [uruk-hai] Uruk-hai, vitality 2; its play requirement does not matter when cheated in
					put("forger", "3_56");      // Isengard Forger: [isengard] Orc, not an Uruk-hai; a Shadow ability only used on purpose
					put("denizen", "11_116");   // Denizen of Moria: [orc] Orc, the bearer of a Shadow armor
					put("gauntlets", "11_109"); // Bladed Gauntlets: Shadow armor; lurker and a skirmish ability
					put("demands1", "2_40");    // Demands of the Sackville-Bagginses: [isengard] support-area condition; acts only when a [shire] ally exerts
					put("demands2", "2_40");
					put("demands3", "2_40");
					put("demands4", "2_40");
					put("demands5", "2_40");
					put("demands6", "2_40");
					put("demands7", "2_40");

					put("aragorn", "1_89");     // Aragorn, Ranger of the North: companion; a maneuver ability only used on purpose
					put("boromir", "1_97");     // Boromir, Son of Denethor: companion; a skirmish ability
					put("gimli", "1_13");       // Gimli, Son of Gloin: Dwarf companion; a skirmish ability
					put("gandalf", "1_72");     // Gandalf, Friend of the Shirefolk: companion; strength by races only
					put("guard", "1_7");        // Dwarf Guard: Dwarf companion with no text but its play requirement

					put("mail", "1_101");       // Coat of Mail: [gondor] armor, bearer a Man
					put("darmor", "1_8");       // Dwarven Armor: [dwarven] armor, bearer a Dwarf
					put("helm", "1_15");        // Gimli's Helm: helm, bearer Gimli
					put("shield", "1_107");     // Great Shield: shield, bearer a Man; minion archery total -1
					put("sword", "1_112");      // Ranger's Sword: hand weapon, bearer Aragorn
					put("athelas", "1_94");     // Athelas: possession with no item class, bearer a [gondor] Man; a fellowship ability
					put("staff", "4_91");       // Gandalf's Staff: an artifact (staff), bearer Gandalf
					put("mithril", "2_105");    // Mithril-coat: a Free Peoples artifact and an armor, bearer Frodo; nothing it does matters in the maneuver phase
					put("merchant", "7_97");    // Gondorian Merchant: an ally, not a companion; his maneuver ability needs the initiative and two cards in hand
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void BeyondtheHeightofMenStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 2
		 * Name: Beyond the Height of Men
		 * Unique: False
		 * Side: Shadow
		 * Culture: Isengard
		 * Twilight Cost: 2
		 * Type: Event
		 * Subtype: Maneuver
		 * Game Text: Exert an Uruk-hai to discard a Free Peoples armor, helm, or shield.  Hinder up to 4 Free Peoples
		 * 	possessions if you can spot 6 companions or 8 [Isengard] cards.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("beyond");

		assertEquals("Beyond the Height of Men", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.ISENGARD, card.getBlueprint().getCulture());
		assertEquals(CardType.EVENT, card.getBlueprint().getCardType());
		assertTrue(scn.HasTimeword(card, Timeword.MANEUVER));
		assertEquals(2, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void ExertsAnUrukhaiToDiscardAFreePeoplesArmorHelmOrShield() throws DecisionResultInvalidException, CardNotFoundException, InvalidCardDefinitionException {
		var scn = GetScenario();

		var beyond = scn.GetShadowCard("beyond");
		var warrior = scn.GetShadowCard("warrior");
		var whu = scn.GetShadowCard("whu");
		var forger = scn.GetShadowCard("forger");
		var denizen = scn.GetShadowCard("denizen");
		var gauntlets = scn.GetShadowCard("gauntlets");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		var mail = scn.GetFreepsCard("mail");
		var darmor = scn.GetFreepsCard("darmor");
		var helm = scn.GetFreepsCard("helm");
		var shield = scn.GetFreepsCard("shield");
		var sword = scn.GetFreepsCard("sword");
		var athelas = scn.GetFreepsCard("athelas");
		var staff = scn.GetFreepsCard("staff");
		var mithril = scn.GetFreepsCard("mithril");
		// Boromir is made an Uruk-hai: the opponent's, and a valid choice for a bare "an Uruk-hai".
		scn.ApplyAdHocModifier(new IsAdditionalRaceModifier(null, scn.GenerateFreepsFilter("name(Boromir)"), null, Race.URUK_HAI));
		// Five companions with Frodo, and two [isengard] cards: too few for the second sentence.
		scn.MoveCompanionsToTable(aragorn, boromir, gimli, gandalf);
		scn.AttachCardsTo(scn.GetRingBearer(), mithril);
		scn.AttachCardsTo(aragorn, shield, sword);
		scn.AttachCardsTo(boromir, mail, athelas);
		scn.AttachCardsTo(gimli, helm, darmor);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToHand(beyond);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, whu, forger, denizen);
		scn.AttachCardsTo(denizen, gauntlets);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		int twilight = scn.GetTwilight();
		assertTrue(scn.ShadowPlayAvailable(beyond));
		scn.ShadowPlayCard(beyond);

		assertTrue(scn.ShadowHasCardChoiceAvailable(warrior, whu, boromir));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(forger));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(whu);
		assertEquals(1, scn.GetWoundsOn(whu));
		assertEquals(0, scn.GetWoundsOn(warrior));
		assertEquals(twilight - 2, scn.GetTwilight());

		assertTrue(scn.ShadowHasCardChoiceAvailable(mail, darmor, helm, shield, mithril));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sword, athelas, staff, gauntlets));
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertEquals(1, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCard(helm);

		assertInDiscard(helm, beyond);
		assertAttachedTo(mail, boromir);
		assertAttachedTo(darmor, gimli);
		assertAttachedTo(shield, aragorn);
		assertAttachedTo(mithril, scn.GetRingBearer());
		assertFalse(scn.IsHindered(sword));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void HindersUpTo4FreePeoplesPossessionsWith6Companions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beyond = scn.GetShadowCard("beyond");
		var warrior = scn.GetShadowCard("warrior");
		var denizen = scn.GetShadowCard("denizen");
		var gauntlets = scn.GetShadowCard("gauntlets");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var gimli = scn.GetFreepsCard("gimli");
		var gandalf = scn.GetFreepsCard("gandalf");
		var guard = scn.GetFreepsCard("guard");
		var mail = scn.GetFreepsCard("mail");
		var darmor = scn.GetFreepsCard("darmor");
		var helm = scn.GetFreepsCard("helm");
		var shield = scn.GetFreepsCard("shield");
		var sword = scn.GetFreepsCard("sword");
		var athelas = scn.GetFreepsCard("athelas");
		var staff = scn.GetFreepsCard("staff");
		// Six companions with Frodo, and one [isengard] card in play.
		scn.MoveCompanionsToTable(aragorn, boromir, gimli, gandalf, guard);
		scn.AttachCardsTo(aragorn, shield, sword);
		scn.AttachCardsTo(boromir, mail, athelas);
		scn.AttachCardsTo(gimli, helm);
		scn.AttachCardsTo(guard, darmor);
		scn.AttachCardsTo(gandalf, staff);
		scn.MoveCardsToHand(beyond);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, denizen);
		scn.AttachCardsTo(denizen, gauntlets);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowPlayCard(beyond);

		assertEquals(1, scn.GetWoundsOn(warrior));
		scn.ShadowChooseCard(shield);
		assertInDiscard(shield);

		assertTrue(scn.ShadowHasCardChoiceAvailable(mail, darmor, helm, sword, athelas));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(staff, gauntlets));
		assertEquals(0, scn.ShadowGetChoiceMin());
		assertEquals(4, scn.ShadowGetChoiceMax());
		scn.ShadowChooseCards(mail, helm, sword, athelas);

		assertTrue(scn.IsHindered(mail));
		assertTrue(scn.IsHindered(helm));
		assertTrue(scn.IsHindered(sword));
		assertTrue(scn.IsHindered(athelas));
		assertFalse(scn.IsHindered(darmor));
		assertFalse(scn.IsHindered(staff));
		assertFalse(scn.IsHindered(gauntlets));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void HindersNothingWith5CompanionsAnd7IsengardCards() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beyond = scn.GetShadowCard("beyond");
		var warrior = scn.GetShadowCard("warrior");
		var whu = scn.GetShadowCard("whu");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var mail = scn.GetFreepsCard("mail");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn, boromir, scn.GetFreepsCard("gimli"), scn.GetFreepsCard("gandalf"));
		// An ally is not a companion: with him there are six characters, but five companions.
		scn.MoveAlliesToTable(scn.GetFreepsCard("merchant"));
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(boromir, mail);
		scn.MoveCardsToHand(beyond);
		// The Orthanc Warrior and six conditions make 7 [isengard] cards; the White Hand Uruk is [uruk-hai].
		scn.MoveShadowCardsToSupportArea("demands1", "demands2", "demands3", "demands4", "demands5", "demands6");

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, whu);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowPlayCard(beyond);
		scn.ShadowChooseCard(warrior);

		// The Coat of Mail is the only armor, helm or shield, and goes without a choice.
		assertInDiscard(mail);
		assertFalse(scn.IsHindered(sword));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void HindersWith8IsengardCardsAndNothingDiscarded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beyond = scn.GetShadowCard("beyond");
		var warrior = scn.GetShadowCard("warrior");
		var aragorn = scn.GetFreepsCard("aragorn");
		var boromir = scn.GetFreepsCard("boromir");
		var sword = scn.GetFreepsCard("sword");
		var athelas = scn.GetFreepsCard("athelas");
		// Five companions with Frodo, and no armor, helm or shield anywhere.
		scn.MoveCompanionsToTable(aragorn, boromir, scn.GetFreepsCard("gimli"), scn.GetFreepsCard("gandalf"));
		scn.AttachCardsTo(aragorn, sword);
		scn.AttachCardsTo(boromir, athelas);
		scn.MoveCardsToHand(beyond);
		// The Orthanc Warrior and seven conditions make 8 [isengard] cards.
		scn.MoveShadowCardsToSupportArea("demands1", "demands2", "demands3", "demands4", "demands5", "demands6", "demands7");

		scn.StartGame();
		scn.MoveMinionsToTable(warrior);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();
		scn.ShadowPlayCard(beyond);

		assertEquals(1, scn.GetWoundsOn(warrior));
		assertTrue(scn.ShadowHasCardChoiceAvailable(sword, athelas));
		assertEquals(0, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(sword);

		assertTrue(scn.IsHindered(sword));
		assertFalse(scn.IsHindered(athelas));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void CannotBePlayedWhenEveryUrukhaiIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var beyond = scn.GetShadowCard("beyond");
		var warrior = scn.GetShadowCard("warrior");
		var whu = scn.GetShadowCard("whu");
		var aragorn = scn.GetFreepsCard("aragorn");
		var shield = scn.GetFreepsCard("shield");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, shield);
		scn.MoveCardsToHand(beyond);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, whu, scn.GetShadowCard("forger"));
		scn.AddWoundsToChar(warrior, 1);
		scn.AddWoundsToChar(whu, 1);

		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// Vitality 2 with 1 wound each; the Isengard Forger could exert, but is not an Uruk-hai.
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertFalse(scn.ShadowPlayAvailable(beyond));
	}
}
