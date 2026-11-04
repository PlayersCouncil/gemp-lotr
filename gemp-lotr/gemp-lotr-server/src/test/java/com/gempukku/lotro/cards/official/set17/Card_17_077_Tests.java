package com.gempukku.lotro.cards.official.set17;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import com.gempukku.lotro.logic.modifiers.StrengthModifier;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_17_077_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("flanker", "17_77");
					put("flanker2", "17_77");
					put("threatening", "17_91"); // Threatening Warg: [orc] mount for an [orc] Orc of strength 10 or less; +2, +1, bearer fierce; its play trigger never set off (cheated on)
					put("vicious", "17_92");     // Vicious Warg: [orc] mount for an [orc] Orc of strength 9 or less; +4, +2, bearer fierce
					put("gear", "17_90");        // Rider's Gear: [orc] armor, no mount, for an [orc] Orc; its bar holds only while bearer is mounted
					put("gothwarg", "10_89");    // Gothmog's Warg: a [sauron] mount, for a [sauron] Orc; strength for sites controlled
					put("brute", "10_90");       // Mordor Brute: a [sauron] Orc; his bar on wounds is never reached
					put("denizen", "11_115");    // Denizen of Khazad-dum: [orc] Orc, vitality 2; +2 only at an underground site
					put("denizen2", "11_115");
					put("halftroll", "14_12");   // Half-troll of Far Harad: [orc] minion who is no Orc; its trigger needs more companions than minions
					put("cavalry", "17_74");     // Orkish Cavalry: [orc] Orc; a draw on playing a mount, and an assignment ability, neither used
					put("troop", "1_177");       // Goblin Patrol Troop: [moria] Orc minion, no text
					put("beckoning", "12_83");   // The Beckoning Shadow: [orc] support-area condition; nothing below 5 burdens
					put("weary", "4_212");       // Weary: [isengard] condition on a companion; its trigger needs an [isengard] tracker
					put("lord", "1_21");         // Lord of Moria: [dwarven] support-area condition; Dwarves damage +1 (no Dwarf is laid)

					put("aragorn", "1_89");      // Aragorn, Ranger of the North: strength 8, vitality 4; a maneuver ability only used on purpose
					put("gandalf", "1_72");      // Gandalf, Friend of the Shirefolk: strength 6, +1 for a Man and +1 for a Hobbit here: 8
					put("sam", "1_311");         // Sam, Son of Hamfast: vitality 4; abilities never used
					put("barliman", "1_70");     // Barliman Butterbur: an ally; a fellowship ability never used
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void OrkishFlankerStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 17
		 * Name: Orkish Flanker
		 * Unique: False
		 * Side: Shadow
		 * Culture: Orc
		 * Twilight Cost: 4
		 * Type: Minion
		 * Subtype: Orc
		 * Strength: 9
		 * Vitality: 2
		 * Site Number: 4
		 * Game Text: Each time this minion wins a skirmish, if it is mounted, exert a companion.<br><b>Shadow:</b> Exert 3 [orc] minions and spot an [orc] mount to discard a condition.
		*/

		var scn = GetScenario();

		var flanker = scn.GetFreepsCard("flanker");

		assertEquals("Orkish Flanker", flanker.getBlueprint().getTitle());
		assertNull(flanker.getBlueprint().getSubtitle());
		assertFalse(flanker.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, flanker.getBlueprint().getSide());
		assertEquals(Culture.ORC, flanker.getBlueprint().getCulture());
		assertEquals(CardType.MINION, flanker.getBlueprint().getCardType());
		assertEquals(Race.ORC, flanker.getBlueprint().getRace());
		assertEquals(4, flanker.getBlueprint().getTwilightCost());
		assertEquals(9, flanker.getBlueprint().getStrength());
		assertEquals(2, flanker.getBlueprint().getVitality());
		assertEquals(4, flanker.getBlueprint().getSiteNumber());
	}

	@Test
	public void EachWinWhileMountedExertsACompanionOfTheShadowPlayersChoosingAndAnUnmountedCopyWinsForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var sam = scn.GetFreepsCard("sam");
		var barliman = scn.GetFreepsCard("barliman");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, gandalf, sam);
		scn.MoveAlliesToTable(barliman);
		// Sam is exhausted: 3 wounds on 4 vitality.
		scn.AddWoundsToChar(sam, 3);

		var flanker = scn.GetShadowCard("flanker");
		var flanker2 = scn.GetShadowCard("flanker2");
		var threatening = scn.GetShadowCard("threatening");
		var gear = scn.GetShadowCard("gear");

		scn.StartGame();
		assertEquals(3, scn.GetWoundsOn(sam));
		scn.MoveMinionsToTable(flanker, flanker2);
		scn.AttachCardsTo(flanker, threatening);
		scn.AttachCardsTo(flanker2, gear);
		assertAttachedTo(threatening, flanker);
		assertAttachedTo(gear, flanker2);
		scn.SkipToAssignments();
		// The mounted Flanker, 9 + 2, on Aragorn, 8; the other, 9 + 2 for its armor, on Gandalf, 8.
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{aragorn, flanker}, new PhysicalCardImpl[]{gandalf, flanker2});
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(frodo));

		scn.PassSkirmishActions();
		// The wound for losing and the Flanker's trigger come at the same moment.
		assertTrue(scn.FreepsAwaitingRequiredResponseOrder());
		scn.FreepsResolveRuleFirst();
		assertEquals(1, scn.GetWoundsOn(aragorn));

		// The Shadow player chooses the companion: any companion able to exert, the loser too; not the exhausted
		// Sam, not the ally, not a minion.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gandalf, frodo));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(sam, barliman, flanker, flanker2));
		scn.ShadowChooseCard(gandalf);
		assertEquals(1, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));
		assertEquals(3, scn.GetWoundsOn(sam));

		// The Flanker in armor wins as well, and nothing is exerted for it: only the wound for losing.
		scn.FreepsResolveSkirmish(gandalf);
		scn.PassSkirmishActions();
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(1, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(frodo));

		// The Warg makes its bearer fierce: it beats Aragorn again, and a companion is exerted again.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
		scn.PassFierceAssignmentActions();
		scn.FreepsAssignToMinions(aragorn, flanker);
		scn.FreepsResolveSkirmish(aragorn);
		scn.PassFierceSkirmishActions();
		scn.FreepsResolveRuleFirst();
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.ShadowHasCardChoiceAvailable(aragorn, gandalf, frodo));
		scn.ShadowChooseCard(frodo);
		assertEquals(1, scn.GetWoundsOn(frodo));
		assertEquals(2, scn.GetWoundsOn(gandalf));
		assertEquals(2, scn.GetWoundsOn(aragorn));
		assertTrue(scn.AwaitingFreepsRegroupPhaseActions());
	}

	@Test
	public void ALossWhileMountedExertsNobody() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var frodo = scn.GetRingBearer();
		scn.MoveCompanionsToTable(aragorn, gandalf);

		var flanker = scn.GetShadowCard("flanker");
		var threatening = scn.GetShadowCard("threatening");

		scn.StartGame();
		scn.MoveMinionsToTable(flanker);
		scn.AttachCardsTo(flanker, threatening);
		assertAttachedTo(threatening, flanker);
		// Aragorn at 13 beats the Flanker, 11, without overwhelming it: one wound of its three.
		scn.ApplyAdHocModifier(new StrengthModifier(null, aragorn, null, 5));
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(aragorn, flanker);
		scn.FreepsResolveSkirmish(aragorn);
		assertEquals(0, scn.GetWoundsOn(flanker));

		scn.PassSkirmishActions();

		assertEquals(1, scn.GetWoundsOn(flanker));
		assertInZone(Zone.SHADOW_CHARACTERS, flanker);
		assertEquals(0, scn.GetWoundsOn(aragorn));
		assertEquals(0, scn.GetWoundsOn(gandalf));
		assertEquals(0, scn.GetWoundsOn(frodo));
		// Still fierce, it is back for the fierce round, and nothing is asked first.
		assertTrue(scn.InFierceRound());
		assertTrue(scn.AwaitingFreepsAssignmentPhaseActions());
	}

	@Test
	public void ShadowAbilityExertsThreeOrcMinionsToDiscardAConditionOfEitherSide() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var gandalf = scn.GetFreepsCard("gandalf");
		var lord = scn.GetFreepsCard("lord");
		scn.MoveCompanionsToTable(aragorn, gandalf);
		scn.MoveCardsToSupportArea(lord);

		var flanker = scn.GetShadowCard("flanker");
		var denizen = scn.GetShadowCard("denizen");
		var denizen2 = scn.GetShadowCard("denizen2");
		var halftroll = scn.GetShadowCard("halftroll");
		var cavalry = scn.GetShadowCard("cavalry");
		var troop = scn.GetShadowCard("troop");
		var threatening = scn.GetShadowCard("threatening");
		var gear = scn.GetShadowCard("gear");
		var beckoning = scn.GetShadowCard("beckoning");
		var weary = scn.GetShadowCard("weary");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, lord);
		scn.FreepsPass();
		scn.MoveMinionsToTable(flanker, denizen, denizen2, halftroll, cavalry, troop);
		scn.AttachCardsTo(denizen, threatening);
		scn.AttachCardsTo(denizen2, gear);
		scn.AttachCardsTo(gandalf, weary);
		scn.MoveCardsToSupportArea(beckoning);
		// The second Denizen is exhausted: 1 wound on 2 vitality.
		scn.AddWoundsToChar(denizen2, 1);
		scn.ShadowRefreshDecision();

		assertAttachedTo(threatening, denizen);
		assertAttachedTo(gear, denizen2);
		assertAttachedTo(weary, gandalf);
		assertInZone(Zone.SUPPORT, lord, beckoning);
		assertEquals(1, scn.GetWoundsOn(denizen2));
		assertEquals(0, scn.GetWoundsOn(flanker));
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertEquals(0, scn.GetWoundsOn(halftroll));
		assertEquals(0, scn.GetWoundsOn(cavalry));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertTrue(scn.ShadowActionAvailable(flanker));

		scn.ShadowUseCardAction(flanker);
		// Three [orc] minions, the Flanker itself among them, of either race; not the [moria] Orc, not the
		// exhausted Denizen.
		assertEquals(3, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(flanker, denizen, halftroll, cavalry));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(troop, denizen2));
		scn.ShadowChooseCards(flanker, denizen, halftroll);
		assertEquals(1, scn.GetWoundsOn(flanker));
		assertEquals(1, scn.GetWoundsOn(denizen));
		assertEquals(1, scn.GetWoundsOn(halftroll));
		assertEquals(0, scn.GetWoundsOn(cavalry));
		assertEquals(0, scn.GetWoundsOn(troop));
		assertEquals(1, scn.GetWoundsOn(denizen2));

		// A condition: the Free Peoples player's, his own in his support area, or one a companion bears; not a
		// possession.
		assertEquals(1, scn.ShadowGetChoiceMin());
		assertTrue(scn.ShadowHasCardChoiceAvailable(lord, beckoning, weary));
		assertTrue(scn.ShadowHasCardChoiceNotAvailable(gear, threatening));
		scn.ShadowChooseCard(lord);

		assertInDiscard(lord);
		assertInZone(Zone.SUPPORT, beckoning);
		assertAttachedTo(weary, gandalf);
		assertAttachedTo(gear, denizen2);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityWithNoConditionInPlayIsPaidForNothing() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		scn.MoveCompanionsToTable(aragorn);

		var flanker = scn.GetShadowCard("flanker");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var vicious = scn.GetShadowCard("vicious");

		scn.StartGame();
		scn.FreepsPass();
		scn.MoveMinionsToTable(flanker, denizen, halftroll);
		scn.AttachCardsTo(denizen, vicious);
		scn.ShadowRefreshDecision();

		assertAttachedTo(vicious, denizen);
		assertEquals(0, scn.GetWoundsOn(flanker));
		assertEquals(0, scn.GetWoundsOn(denizen));
		assertEquals(0, scn.GetWoundsOn(halftroll));
		assertTrue(scn.ShadowActionAvailable(flanker));

		// The only three [orc] minions exert unasked, and there is nothing to discard.
		scn.ShadowUseCardAction(flanker);

		assertEquals(1, scn.GetWoundsOn(flanker));
		assertEquals(1, scn.GetWoundsOn(denizen));
		assertEquals(1, scn.GetWoundsOn(halftroll));
		assertAttachedTo(vicious, denizen);
		assertTrue(scn.AwaitingShadowPhaseActions());
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithNoOrcMountToSpot() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var lord = scn.GetFreepsCard("lord");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(lord);

		var flanker = scn.GetShadowCard("flanker");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var cavalry = scn.GetShadowCard("cavalry");
		var brute = scn.GetShadowCard("brute");
		var gear = scn.GetShadowCard("gear");
		var gothwarg = scn.GetShadowCard("gothwarg");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, lord);
		scn.FreepsPass();
		scn.MoveMinionsToTable(flanker, denizen, halftroll, cavalry, brute);
		// An [orc] possession that is no mount, and a mount that is not [orc].
		scn.AttachCardsTo(denizen, gear);
		scn.AttachCardsTo(brute, gothwarg);
		scn.ShadowRefreshDecision();

		assertAttachedTo(gear, denizen);
		assertAttachedTo(gothwarg, brute);
		assertInZone(Zone.SUPPORT, lord);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(flanker));
	}

	@Test
	public void ShadowAbilityIsNotOfferedWithOnlyTwoOrcMinionsAbleToExert() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var aragorn = scn.GetFreepsCard("aragorn");
		var lord = scn.GetFreepsCard("lord");
		scn.MoveCompanionsToTable(aragorn);
		scn.MoveCardsToSupportArea(lord);

		var flanker = scn.GetShadowCard("flanker");
		var denizen = scn.GetShadowCard("denizen");
		var halftroll = scn.GetShadowCard("halftroll");
		var troop = scn.GetShadowCard("troop");
		var threatening = scn.GetShadowCard("threatening");

		scn.StartGame();
		assertInZone(Zone.SUPPORT, lord);
		scn.FreepsPass();
		scn.MoveMinionsToTable(flanker, denizen, halftroll, troop);
		scn.AttachCardsTo(denizen, threatening);
		// The Half-troll is exhausted: 1 wound on 2 vitality.  The [moria] Orc could exert, but is not [orc].
		scn.AddWoundsToChar(halftroll, 1);
		scn.ShadowRefreshDecision();

		assertAttachedTo(threatening, denizen);
		assertEquals(1, scn.GetWoundsOn(halftroll));
		assertInZone(Zone.SUPPORT, lord);
		assertTrue(scn.AwaitingShadowPhaseActions());
		assertFalse(scn.ShadowActionAvailable(flanker));
	}
}
