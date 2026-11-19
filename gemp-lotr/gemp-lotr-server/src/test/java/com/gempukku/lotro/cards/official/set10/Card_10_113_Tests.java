package com.gempukku.lotro.cards.official.set10;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_10_113_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("armor", "10_113");
					put("pippin", "103_31");    // Pippin, Trollslayer: a [gondor] Hobbit companion, strength 3, enduring;
					                            // his skirmish ability is used only on purpose
					put("bounder", "1_286");    // Bounder: a Hobbit ally, home site 2; a skirmish ability used only on purpose
					put("smeagol", "103_116");  // Smeagol, Once Like Us: a [shire] companion who is no Hobbit
					put("aragorn", "1_89");     // Aragorn, Ranger of the North: a companion who is no Hobbit, strength 8

					put("savage", "1_151");     // Uruk Savage: [isengard] Uruk-hai, strength 5; damage +1 and nothing else
					put("chieftain", "1_266");  // Orc Chieftain: [sauron] Orc, strength 7; its text answers a weapon played on it
					put("brigand", "4_10");     // Dunlending Brigand: [dunland] Man, strength 8, no text
					put("troop", "1_177");      // Goblin Patrol Troop: [moria] Orc, strength 13, no text
					// Morgul Hound: [wraith] Orc, strength 5; +4 and fierce only at 2 wounds on the Ring-bearer, who has none here
					put("hound", "7_192");
					put("cuguard", "10_81");    // Cirith Ungol Guard: [sauron] Uruk-hai, strength 9; damage +1 and nothing else
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrcArmorStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 10
		 * Name: Orc Armor
		 * Unique: False
		 * Side: Free Peoples
		 * Culture: Shire
		 * Twilight Cost: 1
		 * Type: Possession
		 * Subtype: Armor
		 * Game Text: Bearer must be a Hobbit.<br>Each Orc and each Uruk-hai skirmishing bearer is strength -1.
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("armor");

		assertEquals("Orc Armor", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.FREE_PEOPLE, card.getBlueprint().getSide());
		assertEquals(Culture.SHIRE, card.getBlueprint().getCulture());
		assertEquals(CardType.POSSESSION, card.getBlueprint().getCardType());
		assertTrue(card.getBlueprint().getPossessionClasses().contains(PossessionClass.ARMOR));
		assertEquals(1, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void IsPlayedOnAHobbitCompanionOrAllyOfAnyCultureAndNotOnAnotherCharacter() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var frodo = scn.GetRingBearer();
		var pippin = scn.GetFreepsCard("pippin");
		var bounder = scn.GetFreepsCard("bounder");
		var smeagol = scn.GetFreepsCard("smeagol");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(pippin, smeagol, aragorn);
		scn.MoveAlliesToTable(bounder);
		scn.MoveCardsToHand(armor);

		scn.StartGame();
		assertInHand(armor);
		assertTrue(scn.FreepsPlayAvailable(armor));
		scn.FreepsPlayCard(armor);

		// The Ring-bearer, a [gondor] Hobbit companion and a Hobbit ally; not the [shire] companion who is no
		// Hobbit, nor Aragorn.
		assertTrue(scn.FreepsHasCardChoiceAvailable(frodo, pippin, bounder));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(smeagol, aragorn));
		assertEquals(1, scn.FreepsGetChoiceMin());
		scn.FreepsChooseCard(pippin);

		assertAttachedTo(armor, pippin);
		assertTrue(scn.AwaitingFellowshipPhaseActions());
	}

	@Test
	public void EachOrcAndEachUrukHaiSkirmishingBearerIsStrengthMinus1AndNoOtherMinion() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var pippin = scn.GetFreepsCard("pippin");
		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(pippin, aragorn);
		scn.AttachCardsTo(pippin, armor);

		var savage = scn.GetShadowCard("savage");
		var chieftain = scn.GetShadowCard("chieftain");
		var brigand = scn.GetShadowCard("brigand");
		var troop = scn.GetShadowCard("troop");

		scn.StartGame();
		assertAttachedTo(armor, pippin);
		scn.MoveMinionsToTable(savage, chieftain, brigand, troop);
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, troop}, new PhysicalCardImpl[]{pippin, savage});
		// The Shadow player puts the two minions left over on Pippin too.
		scn.ShadowAssignToMinions(pippin, chieftain, brigand);
		scn.FreepsResolveSkirmish(aragorn);

		// Aragorn's skirmish: the Orc fighting him is not skirmishing bearer, and the minions assigned to Pippin
		// are not skirmishing yet.
		assertTrue(scn.IsCharSkirmishingAgainst(aragorn, troop));
		assertEquals(13, scn.GetStrength(troop));
		assertEquals(5, scn.GetStrength(savage));
		assertEquals(7, scn.GetStrength(chieftain));
		assertEquals(8, scn.GetStrength(brigand));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());

		scn.PassSkirmishActions();
		scn.FreepsResolveSkirmish(pippin);

		// Pippin's skirmish: the [isengard] Uruk-hai and the [sauron] Orc are -1; the [dunland] Man is not.
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, savage));
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, chieftain));
		assertTrue(scn.IsCharSkirmishingAgainst(pippin, brigand));
		assertEquals(4, scn.GetStrength(savage));
		assertEquals(6, scn.GetStrength(chieftain));
		assertEquals(8, scn.GetStrength(brigand));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}

	@Test
	public void BorneByAnAllyEachOrcAndUrukHaiSkirmishingHimIsStrengthMinus1() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var armor = scn.GetFreepsCard("armor");
		var bounder = scn.GetFreepsCard("bounder");
		scn.MoveAlliesToTable(bounder);
		scn.AttachCardsTo(bounder, armor);

		var hound = scn.GetShadowCard("hound");
		var cuguard = scn.GetShadowCard("cuguard");

		scn.StartGame();
		assertAttachedTo(armor, bounder);
		scn.MoveMinionsToTable(hound, cuguard);
		scn.SkipToAssignments();
		// Site 2 is Bounder's home: he may be assigned there.
		assertEquals(2, scn.GetCurrentSiteNumber());
		assertEquals(5, scn.GetStrength(hound));
		assertEquals(9, scn.GetStrength(cuguard));
		scn.FreepsAssignToMinions(bounder, hound);
		scn.ShadowAssignToMinions(bounder, cuguard);
		scn.FreepsResolveSkirmish(bounder);

		// A [wraith] Orc and a [sauron] Uruk-hai.
		assertTrue(scn.IsCharSkirmishingAgainst(bounder, hound));
		assertTrue(scn.IsCharSkirmishingAgainst(bounder, cuguard));
		assertEquals(4, scn.GetStrength(hound));
		assertEquals(8, scn.GetStrength(cuguard));
		assertTrue(scn.AwaitingFreepsSkirmishPhaseActions());
	}
}
