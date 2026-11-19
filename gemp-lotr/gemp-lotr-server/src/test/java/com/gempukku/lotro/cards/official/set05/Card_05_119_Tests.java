package com.gempukku.lotro.cards.official.set05;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.LotroCardBlueprint;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_05_119_Tests
{

	// The Towers path with Nan Curunír as site 8.  Site 7 is Helm's Tower (damage +1 only while 6 companions can be
	// spotted; there are never more than three).  The minions are laid at site 7, before the move to site 8: site 6,
	// Caves of Aglarond, is underground and would discard the Black Steeds.
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a [gondor] Man, strength 8, vitality 4; a maneuver ability used only on purpose
					put("sword", "1_112");      // Ranger's Sword: on Aragorn, strength +2 and damage +1
					// Defend It and Hope: Maneuver event, cost 0: exert a [gondor] Man to wound a minion once (twice
					// an Uruk-hai, and none is wounded here)
					put("defend1", "4_115");
					put("defend2", "4_115");
					put("defend3", "4_115");
					put("guard", "5_83");       // Household Guard: a [rohan] Man companion, strength 6; Valiant and no other text
					put("ridersmount", "4_287");// Rider's Mount: a Free Peoples mount; each minion skirmishing bearer exerts at the start of his skirmish

					// Three mounted minions of three cultures, races and mounts.
					put("lemenya", "1_232");    // Úlairë Lemenya: a [wraith] Nazgûl, strength 9, vitality 2; text only on bearers of [wraith] conditions
					put("steed", "1_208");      // Black Steed: a Nazgûl's mount, strength +2, and +2 more at a plains site such as Nan Curunír
					put("otsea", "1_235");      // Úlairë Otsëa: a second Nazgûl, strength 9, vitality 2; a Shadow ability used only on purpose
					put("steed2", "1_208");
					put("wargrider", "5_67");   // Warg-rider: an [isengard] Orc warg-rider, strength 5, vitality 2; fierce while not exhausted
					put("warg", "5_65");        // Warg: a warg-rider's mount, strength +3, vitality +1; a response used only on purpose
					put("soldier", "4_254");    // Southron Soldier: a [raider] Southron Man, strength 4, vitality 1; ambush (2)
					put("mumak", "5_73");       // Mûmak: a Southron's mount, strength +3, fierce; an optional trigger on a skirmish won

					put("troop", "1_177");      // Goblin Patrol Troop: a [moria] Orc, strength 13, vitality 3, no text
					put("scimitar", "1_180");   // Goblin Scimitar: a possession that is no mount, on a [moria] Orc; a trigger on its own play
				}},
				new HashMap<>()
				{{
					putAll(VirtualTableScenario.TowersSites);
					put("site8", "5_119");
				}},
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void NanCurunirStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 5
		 * Name: Nan Curunír
		 * Unique: False
		 * Type: Site
		 * Block: Towers
		 * Site Number: 8
		 * Shadow Number: 8
		 * Direction: Left
		 * Game Text: <b>Plains</b>. <b>Response:</b> If your mounted minion is about to take a wound, remove (2) to prevent that wound.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsSite(8);

		assertEquals("Nan Curunír", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(CardType.SITE, card.getBlueprint().getCardType());
		assertEquals(SitesBlock.TWO_TOWERS, card.getBlueprint().getSiteBlock());
		assertEquals(8, card.getBlueprint().getSiteNumber());
		assertEquals(8, card.getBlueprint().getTwilightCost());
		assertEquals(LotroCardBlueprint.Direction.LEFT, card.getBlueprint().getSiteDirection());
		assertTrue(scn.HasKeyword(card, Keyword.PLAINS));
	}

	@Test
	public void RemovingTwoPreventsAWoundToEachOfThreeMountedMinions() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend1 = scn.GetFreepsCard("defend1");
		var defend2 = scn.GetFreepsCard("defend2");
		var defend3 = scn.GetFreepsCard("defend3");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend1, defend2, defend3);

		var lemenya = scn.GetShadowCard("lemenya");
		var steed = scn.GetShadowCard("steed");
		var wargrider = scn.GetShadowCard("wargrider");
		var warg = scn.GetShadowCard("warg");
		var soldier = scn.GetShadowCard("soldier");
		var mumak = scn.GetShadowCard("mumak");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveMinionsToTable(lemenya, wargrider, soldier);
		scn.AttachCardsTo(lemenya, steed);
		scn.AttachCardsTo(wargrider, warg);
		scn.AttachCardsTo(soldier, mumak);
		scn.SkipToPhase(Phase.MANEUVER);

		var site = scn.GetCurrentSite();
		assertEquals("Nan Curunír", site.getBlueprint().getTitle());
		assertAttachedTo(steed, lemenya);
		assertAttachedTo(warg, wargrider);
		assertAttachedTo(mumak, soldier);
		scn.SetTwilight(7);

		// The Nazgûl on his Black Steed is about to take the wound.
		assertEquals(0, scn.GetWoundsOn(lemenya));
		scn.FreepsPlayCard(defend1);
		scn.FreepsChooseCard(lemenya);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowChooseOptionalTrigger(site);

		assertEquals(5, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPass();

		// The Warg-rider on his Warg.
		assertEquals(0, scn.GetWoundsOn(wargrider));
		scn.FreepsPlayCard(defend2);
		scn.FreepsChooseCard(wargrider);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowChooseOptionalTrigger(site);

		assertEquals(3, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(wargrider));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		scn.ShadowPass();

		// The Southron on his Mûmak: vitality 1, so the wound would have killed him.
		scn.FreepsPlayCard(defend3);
		scn.FreepsChooseCard(soldier);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowChooseOptionalTrigger(site);

		assertEquals(1, scn.GetTwilight());
		assertEquals(0, scn.GetWoundsOn(soldier));
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya, wargrider, soldier);
		assertEquals(3, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
	}

	@Test
	public void NotOfferedForAMinionBearingAPossessionThatIsNoMount() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend1 = scn.GetFreepsCard("defend1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend1);

		var troop = scn.GetShadowCard("troop");
		var scimitar = scn.GetShadowCard("scimitar");
		var lemenya = scn.GetShadowCard("lemenya");
		var steed = scn.GetShadowCard("steed");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveMinionsToTable(troop, lemenya);
		scn.AttachCardsTo(troop, scimitar);
		scn.AttachCardsTo(lemenya, steed);
		scn.SkipToPhase(Phase.MANEUVER);

		var site = scn.GetCurrentSite();
		assertEquals("Nan Curunír", site.getBlueprint().getTitle());
		assertAttachedTo(scimitar, troop);
		assertAttachedTo(steed, lemenya);
		scn.SetTwilight(5);

		// The Troop bears a hand weapon and no mount.  The site could be paid for, and is not offered.
		assertEquals(0, scn.GetWoundsOn(troop));
		scn.FreepsPlayCard(defend1);
		scn.FreepsChooseCard(troop);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(site));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(5, scn.GetTwilight());
	}

	@Test
	public void NotOfferedForAMountedCompanionWhoLosesASkirmish() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var guard = scn.GetFreepsCard("guard");
		var ridersmount = scn.GetFreepsCard("ridersmount");
		scn.MoveCompanionsToTable(guard);
		scn.AttachCardsTo(guard, ridersmount);

		var lemenya = scn.GetShadowCard("lemenya");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveMinionsToTable(lemenya);
		scn.SkipToAssignments();

		var site = scn.GetCurrentSite();
		assertEquals("Nan Curunír", site.getBlueprint().getTitle());
		assertAttachedTo(ridersmount, guard);
		scn.FreepsAssignToMinions(guard, lemenya);
		scn.FreepsResolveSkirmish(guard);
		scn.SetTwilight(5);
		scn.PassSkirmishActions();

		// Lemenya, 9 and exerted by the Rider's Mount, beats the Guard, 6, without doubling him.  The Guard is
		// mounted, and a companion: neither player is offered the site.
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(site));
		assertFalse(scn.FreepsHasOptionalTriggerAvailable(site));
		assertEquals(1, scn.GetWoundsOn(guard));
		assertEquals(5, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WithOneInThePoolTheSiteIsNotOfferedAndTheWoundIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var defend1 = scn.GetFreepsCard("defend1");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToHand(defend1);

		var lemenya = scn.GetShadowCard("lemenya");
		var steed = scn.GetShadowCard("steed");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveMinionsToTable(lemenya);
		scn.AttachCardsTo(lemenya, steed);
		scn.SkipToPhase(Phase.MANEUVER);

		var site = scn.GetCurrentSite();
		assertEquals("Nan Curunír", site.getBlueprint().getTitle());
		assertAttachedTo(steed, lemenya);
		scn.SetTwilight(1);

		assertEquals(0, scn.GetWoundsOn(lemenya));
		scn.FreepsPlayCard(defend1);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(site));
		assertTrue(scn.AwaitingShadowManeuverPhaseActions());
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertEquals(1, scn.GetTwilight());
	}

	@Test
	public void OnlyTheWoundItAnswersIsPreventedAndTheSecondWoundOfALostSkirmishIsTaken() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var sword = scn.GetFreepsCard("sword");
		scn.MoveCompanionsToTable(aragorn);
		scn.AttachCardsTo(aragorn, sword);
		// Aragorn, 8 + 2 + 10, beats Lemenya, 9 + 2 + 2 on her Steed at a plains site, without doubling her.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 10));

		var lemenya = scn.GetShadowCard("lemenya");
		var steed = scn.GetShadowCard("steed");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveMinionsToTable(lemenya);
		scn.AttachCardsTo(lemenya, steed);
		scn.SkipToAssignments();

		var site = scn.GetCurrentSite();
		assertEquals("Nan Curunír", site.getBlueprint().getTitle());
		assertAttachedTo(steed, lemenya);
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(20, scn.GetStrength(aragorn));
		assertEquals(13, scn.GetStrength(lemenya));
		scn.SetTwilight(3);
		scn.PassSkirmishActions();

		// Two wounds for the sword's damage +1: the first is prevented, (2) removed from 3.
		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowChooseOptionalTrigger(site);
		assertEquals(1, scn.GetTwilight());

		// The second has its own window, and (2) cannot be removed from 1: taken.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(site));
		assertEquals(1, scn.GetWoundsOn(lemenya));
		assertInZone(Zone.SHADOW_CHARACTERS, lemenya);
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void WhenSeveralMinionsLoseOneSkirmishTheWoundSavedIsAMountedMinionsAndNoOther() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);
		// Aragorn, 8 + 40, beats the three together, 13 + 13 + 15 = 41, and is far from doubling them: nobody is
		// overwhelmed, and the three lose together and are wounded together.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 40));

		var lemenya = scn.GetShadowCard("lemenya");
		var steed = scn.GetShadowCard("steed");
		var otsea = scn.GetShadowCard("otsea");
		var steed2 = scn.GetShadowCard("steed2");
		var troop = scn.GetShadowCard("troop");
		var scimitar = scn.GetShadowCard("scimitar");

		scn.StartGame();
		scn.SkipToSite(7);
		scn.MoveMinionsToTable(lemenya, otsea, troop);
		scn.AttachCardsTo(lemenya, steed);
		scn.AttachCardsTo(otsea, steed2);
		scn.AttachCardsTo(troop, scimitar);
		scn.SkipToAssignments();

		var site = scn.GetCurrentSite();
		assertEquals("Nan Curunír", site.getBlueprint().getTitle());
		assertAttachedTo(steed, lemenya);
		assertAttachedTo(steed2, otsea);
		assertAttachedTo(scimitar, troop);
		scn.FreepsAssignToMinions(aragorn, lemenya);
		scn.ShadowAssignToMinions(aragorn, otsea, troop);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(48, scn.GetStrength(aragorn));
		scn.SetTwilight(3);
		scn.PassSkirmishActions();

		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(0, scn.GetWoundsOn(otsea));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(site));
		scn.ShadowChooseOptionalTrigger(site);
		assertEquals(1, scn.GetTwilight());

		// The two mounted Nazgûl are offered; the Troop, which bears no mount, is not.
		assertTrue(scn.ShadowHasCardChoiceAvailable(lemenya, otsea));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(lemenya);

		assertEquals(0, scn.GetWoundsOn(lemenya));
		assertEquals(1, scn.GetWoundsOn(otsea));
		assertEquals(1, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetTwilight());
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}
}
