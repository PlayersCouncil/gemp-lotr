package com.gempukku.lotro.cards.official.set08;

import com.gempukku.lotro.framework.VirtualTableScenario;
import com.gempukku.lotro.common.*;
import com.gempukku.lotro.game.CardNotFoundException;
import com.gempukku.lotro.game.PhysicalCardImpl;
import com.gempukku.lotro.logic.decisions.DecisionResultInvalidException;
import org.junit.Test;

import java.util.HashMap;

import static com.gempukku.lotro.framework.Assertions.*;
import static org.junit.Assert.*;

public class Card_08_063_Tests
{

	protected VirtualTableScenario GetScenario() throws CardNotFoundException, DecisionResultInvalidException {
		return new VirtualTableScenario(
				new HashMap<>()
				{{
					put("line", "8_63");

					put("warrior", "4_222");      // Desert Warrior: [raider] Southron Man; a start-of-skirmish trigger that costs (3)
					put("mumak", "5_73");         // Mumak: [raider] mount, "Bearer must be a Southron"; fierce, and a win trigger
					put("halberd", "7_156");      // Raider Halberd: [raider] hand weapon, a possession that is no mount; a Skirmish ability
					put("attacker", "7_141");     // Easterling Attacker: [raider] Easterling Man; a Skirmish ability only
					put("freebooter", "8_54");    // Corsair Freebooter: [raider] Corsair Man; a when-played trigger only
					put("khamul", "103_61");      // Ulaire Attea, Khamul of the East: [raider] Nazgul, no Man; his text needs 3 burdens
					put("brigand", "4_10");       // Dunlending Brigand: [dunland] Man minion, no text
					put("rearguard", "103_43");   // Bladetusk Rearguard: [raider] artifact that becomes a mounted Southron minion, no Man

					put("gimli", "1_13");         // Gimli, Son of Gloin: the Dwarf who exerts for Baruk Khazad
					put("baruk", "5_5");          // Baruk Khazad: Maneuver event, "Exert a Dwarf to wound a minion"
					put("eomer", "4_267");        // Eomer, Third Marshal of Riddermark: [rohan] Man companion
					put("firefoot", "13_126");    // Firefoot: [rohan] mount; its triggers need [rohan] tokens, and there are none
					put("gandalf", "1_72");       // Gandalf, Friend of the Shirefolk: for Long I Fell
					put("fell", "4_97");          // Long I Fell: Skirmish event; the Shadow player may make the Free Peoples player wound a minion
				}},
				VirtualTableScenario.FellowshipSites,
				VirtualTableScenario.FOTRFrodo,
				VirtualTableScenario.RulingRing
		);
	}

	@Test
	public void LineofDefenseStatsAndKeywordsAreCorrect() throws DecisionResultInvalidException, CardNotFoundException {

		/**
		 * Set: 8
		 * Name: Line of Defense
		 * Unique: False
		 * Side: Shadow
		 * Culture: Raider
		 * Twilight Cost: 3
		 * Type: Condition
		 * Subtype: Support area
		 * Game Text: While you can spot a mounted [raider] Man, [raider] Men cannot take wounds (except during skirmishes).
		*/

		var scn = GetScenario();

		var card = scn.GetFreepsCard("line");

		assertEquals("Line of Defense", card.getBlueprint().getTitle());
		assertNull(card.getBlueprint().getSubtitle());
		assertFalse(card.getBlueprint().isUnique());
		assertEquals(Side.SHADOW, card.getBlueprint().getSide());
		assertEquals(Culture.RAIDER, card.getBlueprint().getCulture());
		assertEquals(CardType.CONDITION, card.getBlueprint().getCardType());
		assertTrue(scn.HasKeyword(card, Keyword.SUPPORT_AREA));
		assertEquals(3, card.getBlueprint().getTwilightCost());
	}

	@Test
	public void WithAMountedRaiderManRaiderMenCannotTakeWoundsOutsideSkirmishes() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var baruk = scn.GetFreepsCard("baruk");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(baruk);

		var line = scn.GetShadowCard("line");
		var warrior = scn.GetShadowCard("warrior");
		var mumak = scn.GetShadowCard("mumak");
		var attacker = scn.GetShadowCard("attacker");
		var freebooter = scn.GetShadowCard("freebooter");
		var khamul = scn.GetShadowCard("khamul");
		var brigand = scn.GetShadowCard("brigand");
		scn.MoveCardsToSupportArea(line);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, attacker, freebooter, khamul, brigand);
		scn.AttachCardsTo(warrior, mumak);
		assertInZone(Zone.SUPPORT, line);
		assertInZone(Zone.ATTACHED, mumak);
		assertSame(warrior, mumak.getAttachedTo());
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(gimli));
		assertEquals(0, scn.GetWoundsOn(khamul));
		scn.FreepsPlayCard(baruk);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// The three [raider] Men, the mounted one among them, are not offered; a [raider] Nazgul and a [dunland] Man are.
		assertTrue(scn.FreepsHasCardChoiceAvailable(khamul, brigand));
		assertTrue(scn.FreepsHasCardChoiceNotAvailable(warrior, attacker, freebooter));
		scn.FreepsChooseCard(khamul);

		assertEquals(1, scn.GetWoundsOn(khamul));
		assertEquals(0, scn.GetWoundsOn(warrior));
		assertEquals(0, scn.GetWoundsOn(attacker));
		assertEquals(0, scn.GetWoundsOn(freebooter));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void ARaiderManBearingNoMountAndAMountedRohanManLeaveRaiderMenToBeWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var eomer = scn.GetFreepsCard("eomer");
		var firefoot = scn.GetFreepsCard("firefoot");
		var baruk = scn.GetFreepsCard("baruk");
		scn.MoveCompanionsToTable(gimli, eomer);
		scn.AttachCardsTo(eomer, firefoot);
		scn.MoveCardsToHand(baruk);

		var line = scn.GetShadowCard("line");
		var warrior = scn.GetShadowCard("warrior");
		var halberd = scn.GetShadowCard("halberd");
		var attacker = scn.GetShadowCard("attacker");
		scn.MoveCardsToSupportArea(line);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, attacker);
		scn.AttachCardsTo(warrior, halberd);
		assertInZone(Zone.SUPPORT, line);
		assertSame(warrior, halberd.getAttachedTo());
		assertSame(eomer, firefoot.getAttachedTo());
		scn.SkipToPhase(Phase.MANEUVER);

		assertEquals(0, scn.GetWoundsOn(attacker));
		scn.FreepsPlayCard(baruk);
		assertEquals(1, scn.GetWoundsOn(gimli));

		// Neither the [raider] Man with a hand weapon nor the [rohan] Man on a horse is a mounted [raider] Man.
		assertTrue(scn.FreepsHasCardChoiceAvailable(warrior, attacker));
		scn.FreepsChooseCard(attacker);

		assertEquals(1, scn.GetWoundsOn(attacker));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void AMountedRaiderMinionWhoIsNoManLeavesRaiderMenToBeWounded() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gimli = scn.GetFreepsCard("gimli");
		var baruk = scn.GetFreepsCard("baruk");
		scn.MoveCompanionsToTable(gimli);
		scn.MoveCardsToHand(baruk);

		var line = scn.GetShadowCard("line");
		var warrior = scn.GetShadowCard("warrior");
		var rearguard = scn.GetShadowCard("rearguard");
		scn.MoveCardsToSupportArea(line, rearguard);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior);
		scn.SetTwilight(10);
		assertInZone(Zone.SUPPORT, line, rearguard);
		assertFalse(scn.IsType(rearguard, CardType.MINION));
		scn.SkipToPhase(Phase.MANEUVER);
		scn.FreepsPass();

		// The Rearguard becomes a fierce mounted Southron minion: a mounted [raider] card that is no Man.
		scn.ShadowUseCardAction(rearguard);
		assertTrue(scn.IsType(rearguard, CardType.MINION));
		assertTrue(scn.HasKeyword(rearguard, Keyword.MOUNTED));
		assertEquals(0, scn.GetWoundsOn(warrior));

		scn.FreepsPlayCard(baruk);
		assertEquals(1, scn.GetWoundsOn(gimli));
		assertTrue(scn.FreepsHasCardChoiceAvailable(warrior, rearguard));
		scn.FreepsChooseCard(warrior);

		assertEquals(1, scn.GetWoundsOn(warrior));
		assertTrue(scn.AwaitingFreepsManeuverPhaseActions());
	}

	@Test
	public void DuringASkirmishARaiderManInNoSkirmishTakesAWound() throws DecisionResultInvalidException, CardNotFoundException {
		var scn = GetScenario();

		var gandalf = scn.GetFreepsCard("gandalf");
		var gimli = scn.GetFreepsCard("gimli");
		var fell = scn.GetFreepsCard("fell");
		scn.MoveCompanionsToTable(gandalf, gimli);
		scn.MoveCardsToHand(fell);

		var line = scn.GetShadowCard("line");
		var warrior = scn.GetShadowCard("warrior");
		var mumak = scn.GetShadowCard("mumak");
		var freebooter = scn.GetShadowCard("freebooter");
		scn.MoveCardsToSupportArea(line);

		scn.StartGame();
		scn.MoveMinionsToTable(warrior, freebooter);
		scn.AttachCardsTo(warrior, mumak);
		assertInZone(Zone.SUPPORT, line);
		assertSame(warrior, mumak.getAttachedTo());
		scn.SkipToAssignments();
		scn.FreepsAssignToMinions(new PhysicalCardImpl[]{gandalf, warrior}, new PhysicalCardImpl[]{gimli, freebooter});

		scn.FreepsResolveSkirmish(gandalf);
		// Desert Warrior's start-of-skirmish wound, which costs (3), is declined.
		scn.ShadowDeclineOptionalTrigger();
		assertEquals(0, scn.GetWoundsOn(freebooter));
		scn.FreepsPlayCard(fell);
		scn.ShadowChooseYes();

		// The Corsair waits for Gimli's skirmish and is in none now; during a skirmish he may be wounded.
		assertTrue(scn.FreepsHasCardChoiceAvailable(warrior, freebooter));
		scn.FreepsChooseCard(freebooter);

		assertEquals(1, scn.GetWoundsOn(freebooter));
		assertTrue(scn.AwaitingShadowSkirmishPhaseActions());
	}
}
