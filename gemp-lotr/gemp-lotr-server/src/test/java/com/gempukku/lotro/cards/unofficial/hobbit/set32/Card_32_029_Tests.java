package com.gempukku.lotro.cards.unofficial.hobbit.set32;

import com.gempukku.lotro.common.*;
import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_32_029_Tests
{
	// The Hobbit table: the Hobbit path and Bilbo, Reliable Companion 30_46 as the Ring-bearer. Turn 1's Shadow
	// phase is at The Troll Hoard 30_50 (site 2).
	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("mace", "32_29");

					put("azog", "32_28");       // Azog, Commander of the Wolves Army: [moria] Orc, vitality 3; a Maneuver ability at a battleground
					put("defiler", "31_23");    // Azog, The Defiler: [gundabad] Orc; a trigger on playing a mount
					put("bolg", "31_37");       // Bolg, Servant of Sauron: [sauron] Orc, vitality 3; cheated in, so his play trigger is not met
					put("veteran", "32_43");    // Orkish Veteran: [sauron] Orc, cost 2; text only at site 9 and in the regroup phase
					put("aggressor", "30_39");  // Orkish Aggressor: [gundabad] Orc, cost 2; an Assignment ability not used
					put("malevolent", "33_47"); // Malevolent Orc: [sauron] Orc; an end-of-Shadow-phase take that needs an event in the discard pile
					put("bats", "32_30");       // Dark Bats: [moria] minion, a Creature and no Orc; a Shadow ability not used

					put("thorin", "30_19");     // Thorin, Oakenshield: [dwarven] companion; a Fellowship ability not used
					put("balin", "30_2");       // Balin, Brother of Dwalin: [dwarven] companion; a Fellowship ability not used
					put("dwalin", "30_9");      // Dwalin, Brother of Balin: [dwarven] companion; a Maneuver ability not used
					put("fili", "30_10");       // Fili, Brother of Kili: [dwarven] companion; a Response to a wound not used
					put("gloin", "30_11");      // Glóin, Father of Gimli: [dwarven] companion; a Skirmish ability not used
					put("dain", "32_2");        // Dain Ironfoot: a [dwarven] ally; Thorin is strength +1
				}},
				null, null, null,
				VirtualTableScenario.HobbitDraft
		);
	}

	@Test
	public void AzogsMaceStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 32
		 * Name: Azog's Mace
		 * Unique: True
		 * Side: Shadow
		 * Culture: Moria
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Hand Weapon
		 * Strength: +2
		 * Game Text: Bearer must be an Orc.<br>If bearer is Azog, he is <b>fierce</b>.<br><b>Shadow:</b> Exert bearer twice to play an Orc from your discard pile for each [dwarven] companion over 3.
		 */

		var scn = GetScenario();

		var card = scn.GetFreepsCard("mace");

		assertEquals("Azog's Mace", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertTrue(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.MORIA, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.HAND_WEAPON));
		assertEquals(1, card.getBlueprint().getTwilightCost());
		assertEquals(2, card.getBlueprint().getStrength());
	}

	@Test
	public void PlaysOnAnOrcAndMakesAzogFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mace = scn.GetShadowCard("mace");
		var azog = scn.GetShadowCard("azog");
		var bolg = scn.GetShadowCard("bolg");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCardsToHand(mace);

		scn.StartGame();
		assertInHand(mace);
		scn.SetTwilight(10);
		scn.FreepsPass();
		scn.MoveMinionsToTable(azog, bolg, bats);
		scn.ShadowRefreshDecision();
		assertInHand(mace);
		assertInZone(Zone.SHADOW_CHARACTERS, azog, bolg, bats);
		assertFalse(scn.HasKeyword(azog, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(bolg, Keyword.FIERCE));

		assertTrue(scn.ShadowPlayAvailable(mace));
		scn.ShadowPlayCard(mace);
		// Orcs of two cultures; not Dark Bats, a minion who is no Orc.
		assertTrue(scn.ShadowHasCardChoiceAvailable(azog, bolg));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bats));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(azog);

		assertAttachedTo(mace, azog);
		assertTrue(scn.HasKeyword(azog, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(bolg, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnAzogTheDefilerHeIsFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mace = scn.GetShadowCard("mace");
		var defiler = scn.GetShadowCard("defiler");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(defiler);
		assertFalse(scn.HasKeyword(defiler, Keyword.FIERCE));

		scn.AttachCardsTo(defiler, mace);
		assertAttachedTo(mace, defiler);
		assertTrue(scn.HasKeyword(defiler, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void OnAnotherOrcTheMaceMakesNobodyFierce() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mace = scn.GetShadowCard("mace");
		var azog = scn.GetShadowCard("azog");
		var bolg = scn.GetShadowCard("bolg");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(azog, bolg);
		scn.AttachCardsTo(bolg, mace);
		assertAttachedTo(mace, bolg);

		// Bolg bears it and is no Azog; Azog is in play and does not bear it.
		assertFalse(scn.HasKeyword(bolg, Keyword.FIERCE));
		assertFalse(scn.HasKeyword(azog, Keyword.FIERCE));
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithFourDwarvenCompanionsTheShadowAbilityExertsBearerTwiceToPlayOneOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mace = scn.GetShadowCard("mace");
		var bolg = scn.GetShadowCard("bolg");
		var azog = scn.GetShadowCard("azog");
		var veteran = scn.GetShadowCard("veteran");
		var aggressor = scn.GetShadowCard("aggressor");
		var bats = scn.GetShadowCard("bats");
		var freepsMalevolent = scn.GetFreepsCard("malevolent");
		var thorin = scn.GetFreepsCard("thorin");
		var balin = scn.GetFreepsCard("balin");
		var dwalin = scn.GetFreepsCard("dwalin");
		var fili = scn.GetFreepsCard("fili");
		var dain = scn.GetFreepsCard("dain");
		scn.MoveCompanionsToTable(thorin, balin, dwalin, fili);
		scn.MoveAlliesToTable(dain);

		scn.StartGame();
		scn.MoveCardsToDiscard(veteran, aggressor, bats, freepsMalevolent);
		assertInZone(Zone.DISCARD, veteran, aggressor, bats, freepsMalevolent);
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.MoveMinionsToTable(bolg, azog);
		scn.AttachCardsTo(bolg, mace);
		scn.ShadowRefreshDecision();
		assertAttachedTo(mace, bolg);
		assertEquals(0, scn.GetWoundsOn(bolg));
		assertEquals(0, scn.GetWoundsOn(azog));

		assertTrue(scn.ShadowActionAvailable(mace));
		scn.ShadowUseCardAction(mace);
		// The bearer exerts twice; Azog, another Orc who could, does not.
		assertEquals(2, scn.GetWoundsOn(bolg));
		assertEquals(0, scn.GetWoundsOn(azog));

		// Four [dwarven] companions, one over 3; Bilbo, a companion of another culture, and Dain, a [dwarven] ally,
		// are not counted. Orcs of two cultures; not Dark Bats, a minion who is no Orc, nor the Free Peoples
		// player's Orc in his own discard pile.
		assertTrue(scn.ShadowHasCardChoiceAvailable(veteran, aggressor));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(bats, freepsMalevolent));
		assertEquals(1, scn.ShadowGetChoiceMin());
		scn.ShadowChooseCard(aggressor);

		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		// One Orc only.
		assertInZone(Zone.DISCARD, veteran, bats, freepsMalevolent);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithFiveDwarvenCompanionsTwoOrcsArePlayed() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mace = scn.GetShadowCard("mace");
		var bolg = scn.GetShadowCard("bolg");
		var veteran = scn.GetShadowCard("veteran");
		var aggressor = scn.GetShadowCard("aggressor");
		var bats = scn.GetShadowCard("bats");
		scn.MoveCompanionsToTable("thorin", "balin", "dwalin", "fili", "gloin");

		scn.StartGame();
		scn.MoveCardsToDiscard(veteran, aggressor, bats);
		assertInZone(Zone.DISCARD, veteran, aggressor, bats);
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.MoveMinionsToTable(bolg);
		scn.AttachCardsTo(bolg, mace);
		scn.ShadowRefreshDecision();
		assertAttachedTo(mace, bolg);
		assertEquals(0, scn.GetWoundsOn(bolg));

		scn.ShadowUseCardAction(mace);
		assertEquals(2, scn.GetWoundsOn(bolg));
		scn.ShadowChooseCard(veteran);
		assertInZone(Zone.SHADOW_CHARACTERS, veteran);

		// Two over 3: a second Orc, the one left, played with no question.
		assertInZone(Zone.SHADOW_CHARACTERS, aggressor);
		assertInZone(Zone.DISCARD, bats);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void WithThreeDwarvenCompanionsTheExertionsPlayNoOrc() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mace = scn.GetShadowCard("mace");
		var bolg = scn.GetShadowCard("bolg");
		var veteran = scn.GetShadowCard("veteran");
		var dain = scn.GetFreepsCard("dain");
		// Three [dwarven] companions, Bilbo beside them, and Dain, a [dwarven] ally.
		scn.MoveCompanionsToTable("thorin", "balin", "dwalin");
		scn.MoveAlliesToTable(dain);

		scn.StartGame();
		scn.MoveCardsToDiscard(veteran);
		assertInZone(Zone.DISCARD, veteran);
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.MoveMinionsToTable(bolg);
		scn.AttachCardsTo(bolg, mace);
		scn.ShadowRefreshDecision();
		assertAttachedTo(mace, bolg);
		assertEquals(0, scn.GetWoundsOn(bolg));

		// The count after the "to" comes to none: offered, paid, and nothing played.
		assertTrue(scn.ShadowActionAvailable(mace));
		scn.ShadowUseCardAction(mace);
		assertEquals(2, scn.GetWoundsOn(bolg));
		assertInZone(Zone.DISCARD, veteran);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void NotOfferedWhenBearerCannotExertTwice() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mace = scn.GetShadowCard("mace");
		var bolg = scn.GetShadowCard("bolg");
		var veteran = scn.GetShadowCard("veteran");
		scn.MoveCompanionsToTable("thorin", "balin", "dwalin", "fili");

		scn.StartGame();
		scn.MoveCardsToDiscard(veteran);
		assertInZone(Zone.DISCARD, veteran);
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.MoveMinionsToTable(bolg);
		scn.AttachCardsTo(bolg, mace);
		// Vitality 3 with 1 wound: he can exert once, not twice.
		scn.AddWoundsToChar(bolg, 1);
		scn.ShadowRefreshDecision();
		assertAttachedTo(mace, bolg);
		assertEquals(1, scn.GetWoundsOn(bolg));

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mace));
	}

	@Test
	public void NotOfferedWithNoOrcInHisDiscardPile() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var mace = scn.GetShadowCard("mace");
		var bolg = scn.GetShadowCard("bolg");
		var bats = scn.GetShadowCard("bats");
		var freepsMalevolent = scn.GetFreepsCard("malevolent");
		scn.MoveCompanionsToTable("thorin", "balin", "dwalin", "fili");

		scn.StartGame();
		// A minion who is no Orc in his discard pile, and an Orc in the other player's.
		scn.MoveCardsToDiscard(bats, freepsMalevolent);
		assertInZone(Zone.DISCARD, bats, freepsMalevolent);
		scn.SetTwilight(20);
		scn.FreepsPass();
		scn.MoveMinionsToTable(bolg);
		scn.AttachCardsTo(bolg, mace);
		scn.ShadowRefreshDecision();
		assertAttachedTo(mace, bolg);
		assertEquals(0, scn.GetWoundsOn(bolg));

		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(mace));
	}
}
