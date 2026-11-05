package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_041_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. The Bow is played in
	// the Shadow phase of turn 1, at The Troll Hoard (site 2).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("bow1", "32_41");
					put("bow2", "32_41");

					put("veteran", "32_43");    // Orkish Veteran: [sauron] Orc; its regroup ability not used
					put("malevolent", "33_47"); // Malevolent Orc: [sauron] Orc; an end-of-Shadow-phase offer with no event to take
					put("sauron", "32_45");     // Sauron, The Necromancer: [sauron] Maia, no Orc; his assignment ability not used
					put("assaulting", "32_27"); // Assaulting Goblin: [moria] Orc; a response at a battleground only
					put("scimitar", "1_269");   // Orc Scimitar: a [sauron] possession (hand weapon) for a [sauron] Orc; its skirmish ability not used

					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] companion, bearer of Orcrist; a fellowship ability not used
					put("gloin", "30_11");      // Glóin, Father of Gimli: [dwarven] companion, vitality 3; a skirmish ability not used
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion; a fellowship ability not used
					put("orcrist", "30_17");    // Orcrist: a [dwarven] artifact, hand weapon; a win trigger
					put("axe", "31_1");         // Dwarven Axe: a [dwarven] possession, hand weapon; damage +1 at a mountain only
					put("armor", "33_3");       // Balin's Armor: a [dwarven] possession that is no weapon (armor)
					put("ironweapon", "32_18"); // Iron-Forged Weapon: an [esgaroth] hand weapon a [dwarven] companion bears
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void OrkishBowStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Orkish Bow
		 * Unique: False
		 * Side: Shadow
		 * Culture: Sauron
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Ranged Weapon
		 * Strength: 1
		 * Game Text: Bearer must be a [sauron] Orc.
		 * Bearer is an <b>archer</b>.
		 * When you play this possession, you may discard a [dwarven] weapon. The Free Peoples player may exert its bearer to prevent this.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("bow1");

		assertEquals("Orkish Bow", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.SAURON, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.RANGED_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(1, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnASauronOrcOnlyAndMakesThatBearerAnArcher() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetShadowCard("bow1");
		var veteran = scn.GetShadowCard("veteran");
		var malevolent = scn.GetShadowCard("malevolent");
		var sauron = scn.GetShadowCard("sauron");
		var assaulting = scn.GetShadowCard("assaulting");
		var balin = scn.GetFreepsCard("balin");
		var armor = scn.GetFreepsCard("armor");
		var ironweapon = scn.GetFreepsCard("ironweapon");
		scn.MoveCompanionsToTable(balin);
		scn.MoveCardsToHand(bow);

		scn.StartGame();
		// No [dwarven] weapon in play: a [dwarven] possession that is no weapon and a weapon of another culture.
		scn.AttachCardsTo(balin, armor, ironweapon);
		assertAttachedTo(armor, balin);
		assertAttachedTo(ironweapon, balin);
		assertInZone(Zone.HAND, bow);
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(veteran, malevolent, sauron, assaulting);
		scn.ShadowRefreshDecision();

		assertFalse(scn.HasKeyword(veteran, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(malevolent, Keyword.ARCHER));
		scn.ShadowPlayCard(bow);
		assertTrue(scn.ShadowHasCardChoiceAvailable(veteran, malevolent));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sauron, assaulting));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(veteran);
		assertAttachedTo(bow, veteran);
		assertTrue(scn.HasKeyword(veteran, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(malevolent, Keyword.ARCHER));
		assertFalse(scn.HasKeyword(sauron, Keyword.ARCHER));

		// Nothing the discard could take: no offer.
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(bow));
		assertAttachedTo(armor, balin);
		assertAttachedTo(ironweapon, balin);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WhenPlayedMayDiscardADwarvenWeaponWhichTheFreePeoplesPlayerDeclinesToPrevent() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow1 = scn.GetShadowCard("bow1");
		var bow2 = scn.GetShadowCard("bow2");
		var veteran = scn.GetShadowCard("veteran");
		var malevolent = scn.GetShadowCard("malevolent");
		var thorin = scn.GetFreepsCard("thorin");
		var gloin = scn.GetFreepsCard("gloin");
		var balin = scn.GetFreepsCard("balin");
		var orcrist = scn.GetFreepsCard("orcrist");
		var axe = scn.GetFreepsCard("axe");
		var armor = scn.GetFreepsCard("armor");
		var ironweapon = scn.GetFreepsCard("ironweapon");
		scn.MoveCompanionsToTable(thorin, gloin, balin);
		scn.MoveCardsToHand(bow1);

		scn.StartGame();
		scn.AttachCardsTo(thorin, orcrist);
		scn.AttachCardsTo(gloin, axe);
		scn.AttachCardsTo(balin, armor, ironweapon);
		assertAttachedTo(orcrist, thorin);
		assertAttachedTo(axe, gloin);
		assertAttachedTo(armor, balin);
		assertAttachedTo(ironweapon, balin);
		assertInZone(Zone.HAND, bow1);
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(veteran, malevolent);
		// A second copy already in play makes no offer of its own.
		scn.AttachCardsTo(malevolent, bow2);
		assertAttachedTo(bow2, malevolent);
		scn.ShadowRefreshDecision();

		// Malevolent Orc already bears a ranged weapon: the Bow goes on the Veteran unasked.
		scn.ShadowPlayCard(bow1);
		assertAttachedTo(bow1, veteran);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bow1));
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(bow2));
		scn.ShadowAcceptOptionalTrigger();
		// A [dwarven] possession and a [dwarven] artifact; not a [dwarven] possession that is no weapon, nor a weapon
		// of another culture.
		assertTrue(scn.ShadowHasCardChoiceAvailable(axe, orcrist));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(armor, ironweapon));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(axe);

		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseNo();
		assertInZone(Zone.DISCARD, axe);
		assertEquals(0, scn.GetWoundsOn(gloin));
		assertAttachedTo(orcrist, thorin);
		assertAttachedTo(armor, balin);
		assertAttachedTo(ironweapon, balin);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void TheFreePeoplesPlayerMayExertTheWeaponsBearerToPreventTheDiscard() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetShadowCard("bow1");
		var veteran = scn.GetShadowCard("veteran");
		var thorin = scn.GetFreepsCard("thorin");
		var gloin = scn.GetFreepsCard("gloin");
		var orcrist = scn.GetFreepsCard("orcrist");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(thorin, gloin);
		scn.MoveCardsToHand(bow);

		scn.StartGame();
		scn.AttachCardsTo(thorin, orcrist);
		scn.AttachCardsTo(gloin, axe);
		assertAttachedTo(orcrist, thorin);
		assertAttachedTo(axe, gloin);
		assertInZone(Zone.HAND, bow);
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(veteran);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(bow);
		assertAttachedTo(bow, veteran);
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(orcrist);

		assertEquals(0, scn.GetWoundsOn(thorin));
		assertTrue(scn.FreepsDecisionAvailable("prevent"));
		scn.FreepsChooseYes();
		// Orcrist's own bearer exerts; neither the other Dwarf nor the Bow's bearer does.
		assertEquals(1, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(gloin));
		assertEquals(0, scn.GetWoundsOn(veteran));
		assertAttachedTo(orcrist, thorin);
		assertAttachedTo(axe, gloin);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ThePreventionIsNotOfferedWhenTheWeaponsBearerIsExhausted() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetShadowCard("bow1");
		var veteran = scn.GetShadowCard("veteran");
		var thorin = scn.GetFreepsCard("thorin");
		var gloin = scn.GetFreepsCard("gloin");
		var orcrist = scn.GetFreepsCard("orcrist");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(thorin, gloin);
		scn.MoveCardsToHand(bow);

		scn.StartGame();
		scn.AttachCardsTo(thorin, orcrist);
		scn.AttachCardsTo(gloin, axe);
		// Vitality 3 with 2 wounds: Glóin is exhausted, while Thorin could still exert.
		scn.AddWoundsToChar(gloin, 2);
		assertEquals(2, scn.GetWoundsOn(gloin));
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertAttachedTo(orcrist, thorin);
		assertAttachedTo(axe, gloin);
		assertInZone(Zone.HAND, bow);
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(veteran);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(bow);
		assertAttachedTo(bow, veteran);
		scn.ShadowAcceptOptionalTrigger();
		scn.ShadowChooseCard(axe);

		assertFalse(scn.FreepsDecisionAvailable("prevent"));
		assertInZone(Zone.DISCARD, axe);
		assertEquals(2, scn.GetWoundsOn(gloin));
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertAttachedTo(orcrist, thorin);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void DecliningTheOfferDiscardsNothingAndAnotherPossessionPlayedOffersNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var bow = scn.GetShadowCard("bow1");
		var scimitar = scn.GetShadowCard("scimitar");
		var veteran = scn.GetShadowCard("veteran");
		var malevolent = scn.GetShadowCard("malevolent");
		var thorin = scn.GetFreepsCard("thorin");
		var gloin = scn.GetFreepsCard("gloin");
		var orcrist = scn.GetFreepsCard("orcrist");
		var axe = scn.GetFreepsCard("axe");
		scn.MoveCompanionsToTable(thorin, gloin);
		scn.MoveCardsToHand(bow, scimitar);

		scn.StartGame();
		scn.AttachCardsTo(thorin, orcrist);
		scn.AttachCardsTo(gloin, axe);
		assertAttachedTo(orcrist, thorin);
		assertAttachedTo(axe, gloin);
		assertInZone(Zone.HAND, bow, scimitar);
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(veteran, malevolent);
		scn.ShadowRefreshDecision();

		scn.ShadowPlayCard(bow);
		scn.ShadowChooseCard(veteran);
		assertAttachedTo(bow, veteran);
		assertTrue(scn.ShadowHasOptionalTriggerAvailable(bow));
		scn.ShadowDeclineOptionalTrigger();
		assertAttachedTo(orcrist, thorin);
		assertAttachedTo(axe, gloin);
		assertEquals(0, scn.GetWoundsOn(thorin));
		assertEquals(0, scn.GetWoundsOn(gloin));
		assertTrue(scn.AwaitingShadowPhaseActions());

		// Another [sauron] possession played after the Bow, with the Bow in play: no offer.
		scn.ShadowPlayCard(scimitar);
		scn.ShadowChooseCard(malevolent);
		assertAttachedTo(scimitar, malevolent);
		assertFalse(scn.ShadowHasOptionalTriggerAvailable(bow));
		assertAttachedTo(orcrist, thorin);
		assertAttachedTo(axe, gloin);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}
}
